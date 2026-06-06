package com.feifan.fuckingnjit.monitor

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.util.LruCache
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import com.feifan.fuckingnjit.utils.TodayScheduleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow

class AppUsageManager : AccessibilityService() {

    companion object {
        private const val TAG = "AppUsageManager"

        @Volatile
        private var lastInterventionTime: Long = 0L

        @Volatile
        private var continuousViolationCount: Int = 0

        @Volatile
        private var currentForegroundPkg: String = ""

        @Volatile
        private var isServiceConnected = false

        private val windowIdCache = LruCache<Int, String>(20)
        private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        @Volatile
        private var lastPackageName: String = ""

        @Volatile
        private var lastSwitchTime: Long = System.currentTimeMillis()

        /** 强制阻断名单，触发后直接返回桌面 */
        private val CATEGORY_GROUP_A = setOf("游戏", "影音娱乐", "购物消费")

        /** 通信豁免名单，不强制退出但记录扣分 */
        private val CATEGORY_GROUP_B = setOf("社交通讯", "办公资讯")

        /** 违规总名单，用于课后计算总分 */
        private val ILLEGAL_CATEGORIES = CATEGORY_GROUP_A + CATEGORY_GROUP_B

        private val appLabelCache = ConcurrentHashMap<String, String>()

        private var interceptJob: Job? = null
        private var screenReceiver: BroadcastReceiver? = null

        /**
         * 获取当前前台应用的包名
         *
         * @return 当前前台应用包名字符串，可能为空
         */
        fun getForegroundPackage(): String = currentForegroundPkg

        /**
         * 获取应用显示名称及分类标签
         *
         * 结果格式为"应用名 分类"，带 LRU 缓存避免重复查询 PackageManager。
         *
         * @param context 应用上下文
         * @param pkg 目标应用包名
         * @return 格式化后的应用显示文本
         */

        /**
         * 检测无障碍服务是否处于"假死"状态
         *
         * 假死定义为：系统设置中开关已关闭且内部连接标志位为 false。
         *
         * @param context 应用上下文
         * @return true 表示服务处于假死状态
         */
        fun isServiceZombie(context: Context): Boolean {
            return !isAccessibilitySettingsOn(context) && !isServiceConnected
        }

        /**
         * 检查系统设置中的无障碍服务开关是否已开启
         *
         * @param context 应用上下文
         * @return true 表示本服务的无障碍权限已开启
         */
        fun isAccessibilitySettingsOn(context: Context): Boolean {
            var accessibilityEnabled: Int
            val service = "${context.packageName}/${AppUsageManager::class.java.name}"//无障碍服务名
            try {
                // 首先检查无障碍是否打开
                accessibilityEnabled = Settings.Secure.getInt(
                    context.applicationContext.contentResolver,
                    Settings.Secure.ACCESSIBILITY_ENABLED
                )
            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
                return false
            }
            if (accessibilityEnabled == 1) {
                val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
                //获取所有打开了的无障碍服务
                val settingValue = Settings.Secure.getString(
                    context.applicationContext.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                if (settingValue != null) {
                    mStringColonSplitter.setString(settingValue)
                    while (mStringColonSplitter.hasNext()) {
                        val accessibilityService = mStringColonSplitter.next()
                        if (accessibilityService.equals(service, ignoreCase = true)) {
                            return true
                        }
                    }
                }
            }
            return false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "无障碍服务已连接 (Active)")
        isServiceConnected = true

        // 注册屏幕状态监听（处理锁屏边缘场景）
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                    Log.d(TAG, "屏幕已锁定，触发结算并拆除定时炸弹")
                    // 立即结束前台应用的超时任务，防止息屏状态下被踢回桌面
                    interceptJob?.cancel()
                    // 强制结算当前存活的应用时长
                    serviceScope.launch {
                        settleLastAppDuration("LOCK_SCREEN")
                    }
                }
            }
        }
        registerReceiver(screenReceiver, filter)

        try {
            val rootNode = rootInActiveWindow//获取活跃窗口的根节点
            val pkg = rootNode?.packageName?.toString()
            // 过滤掉系统ui
            if (!pkg.isNullOrEmpty() && pkg != "com.android.systemui") {
                currentForegroundPkg = pkg
                Log.d(TAG, "初始化拉取到前台包名: $pkg")
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取初始包名失败", e)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.w(TAG, "无障碍服务断开连接 (Unbind)")
        // 标记为断开
        isServiceConnected = false
        currentForegroundPkg = ""
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        Log.w(TAG, "无障碍服务被中断 (Interrupt)")
        isServiceConnected = false
        currentForegroundPkg = ""
    }

    override fun onDestroy() {
        serviceScope.cancel()
        screenReceiver?.let { unregisterReceiver(it) }
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isServiceConnected) isServiceConnected = true
        if (event != null) {
            // 只处理窗口内容改变和窗口状态改变事件
            if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
            ) {
                return
            }
            analyzeForegroundWindow()
        }
    }

    /**
     * 分析当前前台窗口组成，识别出最可能的前台应用窗口
     *
     * 过滤掉输入法、辅助功能悬浮窗等非应用层窗口后，
     * 综合考虑焦点状态和屏幕面积选取目标窗口，异步获取其包名。
     */
    private fun analyzeForegroundWindow() {
        val windowList = windows ?: return
        // 过滤掉输入法、辅助功能悬浮窗等非应用层窗口
        val effectiveWindows = windowList.filter {
            it.type != AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY &&
                    it.type != AccessibilityWindowInfo.TYPE_INPUT_METHOD &&
                    it.type != AccessibilityWindowInfo.TYPE_SPLIT_SCREEN_DIVIDER
        }

        if (effectiveWindows.isEmpty()) return

        var targetWindow: AccessibilityWindowInfo? = null
        var maxWindowSize = 0

        // 找出最可能是前台的窗口（综合考虑焦点和面积大小）
        for (window in effectiveWindows) {
            if (window.isFocused || window.isActive) {
                val bounds = Rect()
                window.getBoundsInScreen(bounds)
                val size = bounds.width() * bounds.height()

                if (size >= maxWindowSize) {
                    maxWindowSize = size
                    targetWindow = window
                }
            }
        }

        // 异步获取包名，防止堵塞主线程
        targetWindow?.let { window ->
            serviceScope.launch {
                val packageName = getPackageNameSafely(window)

                if (packageName != null && packageName != currentForegroundPkg && packageName != "com.android.systemui") {
                    currentForegroundPkg = packageName
                    handleAppSwitch(packageName)
                }
            }
        }
    }

    /**
     * 统一处理应用切换逻辑
     *
     * 结算上一个应用的停留时长，并对新上台的前台应用启动实时干预检测。
     *
     * @param newPackageName 新切换到的应用包名
     */
    private suspend fun handleAppSwitch(newPackageName: String) {
        // 立刻结束上一个应用的超时任务
        interceptJob?.cancel()
        // 结算上一个应用的时长
        settleLastAppDuration(newPackageName)
        handleRealTimeIntervention(newPackageName)
    }

    /**
     * 处理前台应用的实时干预逻辑
     *
     * 仅在上课时间且目标应用属于违规分类时触发。
     * 根据当前运行模式的干预配置执行不同级别的动作：
     * 级别1为静默通知，级别2为震动+警告提示，级别3为强阻断（A组应用返回桌面）。
     * 容忍时长随连续违规次数指数衰减，冷却期结束后自动开启下一轮监控。
     *
     * @param pkgName 需要干预的目标应用包名
     */
    private suspend fun handleRealTimeIntervention(pkgName: String) {
        // 检查是否在上课时间
        if (!TodayScheduleManager.isCurrentlyInClass()) return


        // 取消旧任务
        interceptJob?.cancel()

        //绑定新任务
        interceptJob = serviceScope.launch {
            val now = System.currentTimeMillis()
            val timeSinceLastAction = now - lastInterventionTime

            // 容忍度梯度递减
            // 每次违规，容忍时长变为上一次的 70%
            val decayFactor = 0.7.pow(continuousViolationCount.toDouble())


            // 设定容忍度保底底线：最少 1 分钟
            val minToleranceMs = 1 * 60 * 1000L


            // 超时执行动作
            withContext(Dispatchers.Main) {

            }

            // 更新最后干预时间并增加"仇恨值"
            lastInterventionTime = System.currentTimeMillis()
            continuousViolationCount++

            // 立即开启下一轮监控 (依然在前台则继续倒计时)
            handleRealTimeIntervention(pkgName)
        }
    }

    /**
     * 触发震动反馈
     *
     * 构造三连短震节奏模式并执行振动，用于干预提醒。
     */
    private fun triggerVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

            // 定义节奏模式
            val singlePatternTimings = longArrayOf(0, 100, 100, 100, 100, 100, 200, 400)
            val singlePatternAmplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 200)

            // 重复次数
            val repeatCount = 3

            // 构造重复3次的总数组
            val totalTimings = mutableListOf<Long>()
            val totalAmplitudes = mutableListOf<Int>()

            for (i in 0 until repeatCount) {
                // 第一次循环需要保留开头的 0 等待，后续循环则直接衔接（不需要开头的 0 等待）
                if (i == 0) {
                    totalTimings.addAll(singlePatternTimings.toList())
                    totalAmplitudes.addAll(singlePatternAmplitudes.toList())
                } else {
                    // 跳过开头的 0 等待，直接从第一个震动指令开始衔接
                    totalTimings.addAll(singlePatternTimings.drop(1))
                    totalAmplitudes.addAll(singlePatternAmplitudes.drop(1))
                }
            }

            val effect = VibrationEffect.createWaveform(
                totalTimings.toLongArray(),
                totalAmplitudes.toIntArray(),
                -1  // -1 表示不重复，播放完整个数组即停止
            )

            vibrator.vibrate(effect)
        } catch (e: Exception) {
            Log.e(TAG, "紧急震动调用失败", e)
        }
    }

    /**
     * 结算上一个应用的停留时长
     *
     * 停留超过5秒才计入摸鱼时间。如果上一个应用属于违规分类且当前处于上课时段，
     * 则将违规时长累加到今日记录并保存单节课的专注度明细。
     *
     * @param newPackageName 新切换到的应用包名
     */
    private suspend fun settleLastAppDuration(newPackageName: String) {
        val now = System.currentTimeMillis()
        val durationMs = now - lastSwitchTime
        // 停留超过5秒算摸鱼
        if (lastPackageName.isNotEmpty() && durationMs > 5000) {

            val currentClass = TodayScheduleManager.getCurrentClassSlot()

            if (currentClass != null) {

            }
        }

        // 无论是否违规，更新时间和包名为新应用，开启下一轮计时
        lastPackageName = newPackageName
        lastSwitchTime = now
    }

    /**
     * 安全地获取窗口对应的包名
     *
     * 优先从 LRU 缓存读取，缓存未命中时尝试获取根节点包名并写入缓存。
     * 所有异常情况均返回 null 而非抛出。
     *
     * @param window 目标窗口信息
     * @return 包名字符串，获取失败时返回 null
     */
    private fun getPackageNameSafely(window: AccessibilityWindowInfo): String? {
        val windowId = window.id

        // 命中缓存直接返回
        windowIdCache.get(windowId)?.let { return it }
        return try {
            val rootNode = window.root
            val pkgName = rootNode?.packageName?.toString()
            if (pkgName != null) {
                windowIdCache.put(windowId, pkgName)
            }
            pkgName
        } catch (e: Exception) {
            e.printStackTrace()
            // 获取 root 节点可能会超时或抛出异常
            null
        }
    }
}