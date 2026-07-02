# CLAUDE.md

本文件为 Claude Code（claude.ai/code）在此仓库中工作时提供指导。

<!-- CODEGRAPH_START -->
## CodeGraph

In repositories indexed by CodeGraph (a `.codegraph/` directory exists at the repo root), reach for it BEFORE grep/find or reading files when you need to understand or locate code:

- **MCP tool** (when available): `codegraph_explore` answers most code questions in one call — the relevant symbols' verbatim source plus the call paths between them, including dynamic-dispatch hops grep can't follow. Name a file or symbol in the query to read its current line-numbered source. If it's listed but deferred, load it by name via tool search.
- **Shell** (always works): `codegraph explore "<symbol names or question>"` prints the same output.

If there is no `.codegraph/` directory, skip CodeGraph entirely — indexing is the user's decision.
<!-- CODEGRAPH_END -->

## 构建命令

```bash
# Debug 构建
./gradlew assembleDebug

# Release 构建
./gradlew assembleRelease

# 仅构建 library 模块
./gradlew :fuckingNJIT:assembleDebug

# 清理构建
./gradlew clean
```

项目没有配置常规测试套件。仅在 `fuckingNJIT/src/androidTest/java/com/feifan/fuckingnjit/ExampleInstrumentedTest.kt` 有一个占位插桩测试。

## 架构

这是一个面向 NJIT（南京工程学院）学生的 Android 校园生活管理应用，包含两个 Gradle 模块：

### `app/` — UniApp 外壳
应用入口模块（`uni.UNI2090008`，applicationId `uni.UNI2090008`）。继承自 DCloud 的 `PandoraEntryActivity`（一个混合应用框架，UI 层使用 JavaScript/Vue 构建，运行在 DCloud 的 WebView 容器中）。`LauncherActivity.onCreate()` 调用 `CoreInitializer.init()` 后交由 UniApp 运行时接管。此模块不包含业务逻辑——它只是一个承载 UniApp 前端并声明 `:fuckingNJIT` 库依赖的薄壳。集成了友盟（Umeng）数据分析和推送 SDK。

### `fuckingNJIT/` — 核心库
所有原生 Android 逻辑位于此模块（`com.feifan.fuckingnjit`）。JS 前端通过 `index.kt`（`uts.sdk.modules.fuckingNJIT`）中定义的 UTS 桥接层调用此模块。

**UTS 桥接层（`index.kt`）：**
- `Core` 类以返回 `UTSPromise` 的函数形式暴露 API，`CoreByJs` 为其 JS 侧适配包装。
- 对外暴露的方法包括：`startLogin`、`getCurriculum`、`getAllUsers`/`setCurrentUser`/`deleteUser`、`getEmptyClassrooms`、`getSorces`/`getSorcesDetail`、`getAllExam`、`getAcademicProgress`、`saveCourse`/`deleteCourse`/`restoreCourse`、`getDate`、小组件管理、WiFi 认证控制、`updateApp`、`initYiBan` 等。
- `CourseParams`、`DeleteCourse`、`RestoreCourse` 等参数类定义在此文件中，并有对应的 `*JSONObject` 子类供 JS 使用。

**核心心跳服务：**
- **`CoreService`** — 基于 `LifecycleService` 的前台服务，是应用的心跳引擎。注册为低优先级常驻通知（`IMPORTANCE_LOW`），通过 `AlarmManager.setWindow()` 以非精确闹钟方式周期性触发。每次 tick 通过 `HeartbeatBus` 计算下次唤醒时间并安排。监听屏幕解锁/锁屏事件，解锁时立即触发一次 tick，tick 指向 `CurriculumsWidgetProvider` 广播。
- **`HeartbeatBus`** — 全局心跳调度总线。维护一个关键时间节点的优先队列，支持注册插队节点。`calculateNextTickTime()` 返回基础间隔（60s）和最早关键节点中较近的时间，同时清理已过期节点。广播 Action 为 `ACTION_GLOBAL_TICK`。
- **`Engine`**（widget 包）— 小组件引擎入口。`pingEngine()` 检查 `CoreService` 运行状态，未运行时拉起前台服务并注入 `ACTION_GLOBAL_TICK`。

**教务系统集成：**
- `WebService` 接口 — 定义教务系统功能抽象：`getCurriculum`、`getUserData`、`getSemesterStartDate`、`getEmptyClassrooms`、`getAllSorces`、`getSorcesDetail`、`getAcademicProgress`。
- `WebServiceImpl` — 单例实现。通过 WebVPN 隧道（`BASE_URL = https://casb.njit.edu.cn` + 固定的 `WEBVPN_PATH`）使用 OkHttp + Jsoup 爬取 NJIT 教务系统。除接口方法外，额外提供：`getAllExam`（考试信息查询）、`saveCourse`（保存课程，支持隐藏原系统课程）、`deleteCourse`（删除系统课程时仅添加隐藏规则，本地课程直接物理删除）、`restoreCourse`（移除隐藏规则）、`getDate`（返回学期开始日期和当前周次）。所有网络请求统一经过 `makeRequest()` 进行登录态校验（每 60 秒检测一次 Cookie 是否过期），未授权时自动弹出登录页面。
- `CourseParser` — 将教务系统原始 JSON 解析为 `Course` 实体。处理中文星期名称、时间段正则（`星期X第M-N节{周次}`）、单双周模式、多教师姓名清洗。
- `CourseManager` — 本地课程与隐藏规则管理。本地课程以 `local_` 前缀 UUID 为 key 存入 User 的 `localCurriculums` JSON 树；隐藏规则以 `课程ID@星期@节次` 格式存储在 `hidden_rules_map` 中。课程获取时自动过滤隐藏规则并合并本地课程。
- `ScoreManager` — 成绩数据解析与加权平均绩点（GPA）计算。
- `UserManager` 接口 / `UserManagerImpl` — 多用户账号管理。`addUser()` 为核心方法：并行获取学期开始日期、用户基础信息和成绩，然后持久化到 ObjectBox 并强制拉取课表。支持成绩/课表/学业进度的缓存-刷新模式。密码经 RSA 加密后存储，支持开关密码本地保留。

**登录流程：**
- `SampleWebViewImpl` — 继承 `PandoraEntryActivity` 的 WebView Activity，加载 CAS 登录页面，通过 `SampleWebViewClientImpl` 处理 Cookie 注入和登录成功回调。
- `SystemActionHelper.startLogin()` — 统一登录入口。清空 Cookie、标记登录状态（`AppConfig.inLogin`），启动 `SampleWebViewImpl`。

**数据层（`utils/database/`）：**
- `AppDataCenter` — 封装 ObjectBox `BoxStore` 的单例。仅管理两个 Box：`AppSystem`（单例配置实体，id=1）和 `User`（多用户支持）。数据库统一存储在 `core_database` 文件夹。
- `CoreInitProvider` — 实现 AndroidX `Initializer` 接口，在应用启动早期自动调用 `AppDataCenter.init()` 并执行 `DbClearHelper.checkAndClear()` 做数据库版本清理。
- `DbClearHelper` — 基于数据库版本标记的数据清理工具，支持从 assets 加载 SQL 脚本执行。
- ObjectBox schema 由 `model/` 中标注 `@Entity` 的数据类定义（`AppSystem`、`User`），通过 ObjectBox Gradle 插件代码生成到 `MyObjectBox`。

**数据模型（`model/`）：**
- `AppSystem` — 全局单例配置实体。字段：`currentUserId`、`semesterStartDateMs`、`currentWeek`、`wifiAuthType`（校园网认证策略）、`smartUpdate`（智能更新开关）。
- `User` — 用户实体。字段：`id`（学号，带索引）、`password`（RSA 加密）、`name`、`yibanId`/`yibanPassword`（易班凭据，RSA 加密）、`storePassword`、`gpa`、`academicProgress`（JSONObject）、`scores`（JSONArray）、`curriculums`（课表 JSONObject）、`localCurriculums`（本地课程与隐藏规则 JSONObject）。
- `Course` — 课程数据类（非 ObjectBox 实体），带 FastJSON 注解。字段：id、name、teacher、classroom、day、startNode、step、weekList、source、rawWeeks。

**网络层（`utils/network/`）：**
- `HttpRequestHelper` — OkHttp 客户端单例，支持 Cookie 持久化（通过 `CookieManager`）、10s 连接 / 15s 读写超时。提供 `getJsonResponse()` 和 `getHtmlResponse()` 两个对外入口，内部自动完成 WebVPN Cookie 注入和登录态校验（每 60 秒 Jsoup 探测教务首页）。支持 GET / POST（表单 / JSON Body）三种请求方式。额外提供 `downloadFile()` 用于增量更新下载。
- `NetworkStatus` — 密封类，覆盖标准 HTTP 状态码（2xx/4xx/5xx）和自定义错误码（600 网络不可用、601 解析失败、699 未知错误）。提供 `toJsonResult(data)` 方法快速构建 `{code, message, data}` 格式的统一响应。
- `ApiException` — 携带 `NetworkStatus` 的网络异常类。
- `HttpMethod` — 简单的 GET/POST 枚举。
- `PortalManager`/`PortalActivity` — 校园 WiFi 的 Captive Portal 自动认证（`wifiauth/` 子包）。

**安全（`utils/security/`）：**
- `SecureUtil` — 基于 Android KeyStore 的 RSA-2048 加密工具。密钥别名 `fuckingnjit_key`，使用 ECB 模式 + PKCS1Padding。提供 `rsaEncrypt()`/`rsaDecrypt()` 方法。
- `RSAPasswordConverter` — ObjectBox `PropertyConverter` 实现，在持久化时自动对密码字段进行 RSA 加解密。

**关键工具类：**
- `EduScheduleConfig` — 课程节次时间表（11 节课，每节精确的开始/结束时间）、节次与物理时间的互转、教学周次计算（`calculateWeek`/`calculateCurrentWeek`）、学年学期标识符计算（`getCurrentSchoolYear()`，返回 `"2024-2025-3"` 格式）。
- `TodayScheduleManager` — 从用户缓存的课表数据中过滤出当日课程，转换为带物理时间的 `DailyCourseSlot` 列表。内部按天缓存，提供 `getRemainingCoursesForWidget()` 和 `getCurrentWeek()`。
- `Tools` — 通用工具集：`getTargetSleepWindow()`（返回昨天中午 12:00 → 今天中午 12:00 的时间戳窗口）、`todayWeekIndex()`、`dateChangeSimple()`（日期范围 → 周次/星期映射，用于空教室查询）。
- `Manager` — 服务定位器：`getPermissionsManager()`、`getUserManager()`、`getWebService()`。
- `CoreInitializer` — 根据已保存的学期开始时间计算并恢复当前教学周次；提供 `initYiBan()` 挂起函数验证易班凭据并启用 `KillYiBan` 广播接收器。
- `AppConfig` — 应用全局内存态配置：登录状态标记（`inLogin`）、登出、WiFi 认证策略读写。
- `SystemActionHelper` — 系统 UI 操作工具集：Toast 提示、Loading 弹窗管理、统一异常处理、启动登录、返回桌面、应用增量更新（bsdiff/bspatch 方案，通过 `FileProvider` 安装）。
- `J2J` — JSON 对象转换工具。

**小组件系统（`widget/`）：**
- `CurriculumsWidgetProvider` — 桌面 AppWidget，显示当日课程。通过 `ACTION_GLOBAL_TICK` 广播刷新，由 `CoreService` 的心跳和屏幕解锁事件驱动。
- `CurriculumsWidget` — 供 UTS 桥接层调用的小组件 Helper，封装小组件创建、权限检查、已创建判断等操作。
- `CurriculumsWidgetConfigActivity` — 小组件配置界面。
- `BaseWidgetBridge` / `BaseWidgetConfigActivity` — 小组件基类，封装常用的 PendingIntent 构建和配置流程。
- `Engine` — 小组件引擎入口，负责按需拉起 `CoreService`。

**教务辅助（`utils/academic/`）：**
- `KillYiBan` — 广播接收器，用于处理易班签到相关的自动化任务。
- `TestCourseGenerator` — 测试课程数据生成器（仅在调试时使用，默认注释掉）。

**系统工具（`utils/system/`）：**
- `PermissionsManager` — 基于 XXPermissions 的权限管理。支持安装未知应用权限的检查与申请、智能更新开关的读写。
- `XiaomiUtilities` — 小米设备特有的系统工具。

## 关键依赖

- **ObjectBox** — 嵌入式 NoSQL 数据库。Gradle 插件版本 4.3.0，Kotlin 绑定 5.0.0。实体定义在 `model/` 中。
- **OkHttp 4.12.0** — HTTP 客户端
- **Jsoup 1.21.2** — 用于教务系统爬取的 HTML 解析
- **FastJSON 1.2.83** — JSON 序列化（阿里巴巴）
- **DCloud UniApp** — 混合应用框架（`.aar` 文件位于 `app/libs/` 和 `uniappx/libs/`）
- **友盟（Umeng）** — 数据分析/推送 SDK（仅在 `app/` 模块中集成）
- **XXPermissions 26.5** — 运行时权限申请辅助库
- **FYIBAN 1.0.1** — 自定义易班登录库
- **SmartUpdateDemo 1.0.2** — bsdiff/bspatch 增量更新库
- **Android-Loading-Animation 1.0.6** — Loading 动画库
- **DeviceCompat 2.0** — 设备适配库
- **AndroidX Startup 1.2.0** — 用于 `CoreInitProvider` 的自动初始化
- **AndroidX Lifecycle** — runtime-ktx + service 2.10.0（`CoreService` 使用 `LifecycleService`）
- **Zip4j 2.11.5** — ZIP 文件处理（仅 `app/` 模块）
- **Kotlin 2.2.0**、**AGP 8.11.0**、compileSdk 36、minSdk 26、targetSdk 34

## 代码模式

- **单例对象**（`object` 关键字）优先用于服务和管理器（如 `AppDataCenter`、`EduScheduleConfig`、`TodayScheduleManager`）。需要传参的类（如 `PermissionsManager`）使用 `companion object` + `@Volatile` 双重检查锁实现懒汉单例。
- **协程**用于所有异步操作（网络、数据库）。阻塞类函数应声明为 `suspend` 并使用 `withContext(Dispatchers.IO)`。
- **UTS 桥接**：`index.kt` 中的 `Core` 类使用 `wrapUTSPromise` 将挂起函数包装为 `UTSPromise`；`CoreByJs` 提供 `toDeferred` 包装供 JS 侧调用。参数类（如 `CourseParams`）需同时提供对应的 `*JSONObject` 子类。
- **ObjectBox 实体**在 `model/` 中使用 `@Convert` 注解配合自定义 `PropertyConverter` 处理复杂类型（JSONObject、JSONArray、RSA 加密密码）。非实体的数据类（如 `Course`）使用 FastJSON 的 `@JSONField` 注解。
- **网络响应**遵循 `NetworkStatus.Success.toJsonResult(data)` / `NetworkStatus.UnknownError.toJsonResult(message)` 模式，返回包含 `code`、`message`、`data` 字段的 `JSONObject`。
- **教务系统访问**统一通过 `HttpRequestHelper` 的 `getJsonResponse()` / `getHtmlResponse()` 方法，内部自动处理 Cookie 注入和登录态校验，无需手动管理会话。
- **课程缓存模式**：`UserManagerImpl` 中的 `getUserScores()`、`getCurriculum()`、`getAcademicProgress()` 均采用"优先缓存、可强制刷新"的策略，数据以 JSON 字段存储在 User 实体中。
- **优先使用 CodeGraph** 进行代码探索——此仓库已建立索引。

## Maven 仓库镜像

项目在 `settings.gradle.kts`（或 `settings.gradle`）中配置了阿里云镜像。如果依赖无法从默认仓库拉取，请检查 `maven.aliyun.com` 是否可达，或临时移除镜像配置。
