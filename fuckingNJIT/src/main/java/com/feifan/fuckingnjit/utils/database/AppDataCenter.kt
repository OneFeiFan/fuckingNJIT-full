package com.feifan.fuckingnjit.utils.database

import android.content.Context
import com.feifan.fuckingnjit.model.AppSystem
import com.feifan.fuckingnjit.model.MyObjectBox
import com.feifan.fuckingnjit.model.User
import com.feifan.fuckingnjit.model.User_
import com.feifan.fuckingnjit.utils.database.AppDataCenter.getSystemConfig
import com.feifan.fuckingnjit.utils.database.AppDataCenter.init
import io.objectbox.BoxStore
import io.objectbox.query.QueryBuilder

/**
 * 应用数据访问中心，基于 ObjectBox 本地数据库提供统一的读写入口。
 *
 * 管理五类核心数据：
 * - **系统配置** ([AppSystem])：全局唯一记录（id=1），存储当前激活用户 ID 等应用级状态
 * - **用户信息** ([User])：多租户支持，每个登录过的用户各存一条
 * - **每日记录** ([DailyRecord])：按日期键值对存储，含睡眠、步数、专注度等综合数据
 * - **睡眠传感器** ([SleepSensorRecord])：高频原始采样点（加速度/音频），按时间戳索引
 * - **课程专注度** ([ClassFocusRecord])：每节课一条，支持上传状态标记与批量同步
 *
 * 所有 Box 实例采用 lazy 延迟初始化，物理数据库统一归拢到 `core_database` 文件夹。
 *
 * 典型调用方式：先调用 [init] 初始化数据库，
 * 其余位置通过 companion 方法直接操作，无需手动管理事务。
 */
@Suppress("unused")
object AppDataCenter {
    private var boxStore: BoxStore? = null

    /** 全局系统配置 Box，单例实体 id 固定为 1L */
    private val systemBox by lazy { boxStore!!.boxFor(AppSystem::class.java) }

    /** 用户信息 Box，支持多用户共存 */
    private val userBox by lazy { boxStore!!.boxFor(User::class.java) }

    /** 每日综合健康记录 Box，以 dateStr 为查询键 */

    /**
     * 初始化 ObjectBox 数据库实例。
     *
     * 使用 ApplicationContext 避免内存泄漏
     * 多次调用不会重复创建 BoxStore。必须在任何数据操作之前调用。
     *
     * @param context Application Context
     */
    fun init(context: Context) {
        if (boxStore == null) {
            boxStore = MyObjectBox.builder()
                .androidContext(context.applicationContext)
                .name("core_database") // 所有数据归拢到同一个物理文件夹！
                .build()
        }
    }

    /**
     * 获取底层的 ObjectBox [BoxStore] 实例。
     *
     * 仅在需要跨 Box 联合查询或运行原生事务时使用，常规操作应优先调用本类提供的类型安全方法。
     *
     * @return 已初始化的 BoxStore，未调用 [init] 时返回 null
     */
    fun getBoxStore(): BoxStore? = boxStore

    /**
     * 获取全局系统配置实体。
     *
     * 若数据库中尚不存在（首次启动），会自动创建默认实例（id=1L）并持久化。
     * 该实体是单例的，全局只有一条记录。
     *
     * @return 系统配置对象，永不为 null
     */
    fun getSystemConfig(): AppSystem {
        var sys = systemBox.get(1L) // 全局数据默认分配id为1
        if (sys == null) {
            sys = AppSystem(id = 1L)
            systemBox.put(sys)
        }
        return sys
    }

    /**
     * 以读写事务方式更新系统配置。
     *
     * 内部先取出当前实体，执行 [updater] 回调修改字段，然后写回数据库。
     * 相比直接调用 [getSystemConfig] 再手动 put，此方法更简洁且保证原子性。
     *
     * @param updater 配置修改回调，接收当前可变 AppSystem 实例
     */
    fun updateSystemConfig(updater: (AppSystem) -> Unit) {
        val sys = getSystemConfig()
        updater(sys)
        systemBox.put(sys)
    }

    /**
     * 根据系统配置中的 [AppSystem.currentUserId] 获取当前激活的用户实体。
     *
     * @return 用户对象，未设置当前用户或数据库中找不到对应记录时返回 null
     */
    fun getCurrentUser(): User? {
        val userId = getSystemConfig().currentUserId
        if (userId.isEmpty()) return null
        return userBox.query().equal(User_.id, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build().findFirst()
    }

    /**
     * 新增或更新用户信息（upsert 语义）。
     *
     * 若传入实体的 ObjectBox ID 与已有记录一致则覆盖，否则作为新记录插入。
     *
     * @param user 待保存的用户实体
     */
    fun saveUser(user: User) {
        userBox.put(user)
    }

    /** 获取数据库中所有用户记录，通常用于账号切换或管理界面 */
    fun getAllUsers(): List<User> = userBox.all

    /**
     * 从数据库中删除指定用户记录。
     *
     * 注意：仅删除 User 实体本身，不会级联清理其关联的 DailyRecord 或 ClassFocusRecord，
     * 如需完整清除请额外调用相关清理方法。
     *
     * @param user 待删除的用户实体
     */
    fun deleteUser(user: User) {
        userBox.remove(user)
    }
}