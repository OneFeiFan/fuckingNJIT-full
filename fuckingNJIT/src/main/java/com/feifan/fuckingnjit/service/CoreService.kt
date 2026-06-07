package com.feifan.fuckingnjit.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.feifan.fuckingnjit.R
import com.feifan.fuckingnjit.utils.HeartbeatBus
import com.feifan.fuckingnjit.widget.CurriculumsWidgetProvider

class CoreService : LifecycleService() {

    companion object {
        var isRunning = false
            private set // 外部（小部件）只能读，内部（Service）可以改
    }

    private val TAG = "CoreService"

    private val CHANNEL_ID = "AlwaysOnChannel"

    private val NOTIFICATION_ID = 1
    private lateinit var alarmManager: AlarmManager
    private var lastScreenState = "点亮 (ON)"
    private lateinit var screenReceiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 及以上必须创建 NotificationChannel
        val channel = NotificationChannel(
            CHANNEL_ID,
            "核心心跳引擎", // 用户在系统设置里看到的通道名字
            NotificationManager.IMPORTANCE_LOW // 使用 LOW 级别，不会发出声音和弹窗打扰用户
        ).apply {
            description = "维持课程表小部件的后台状态更新"
        }
        notificationManager.createNotificationChannel(channel)

        // 构建通知卡片
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("引擎运行中")
            .setContentText(lastScreenState) // 可以展示当前的屏幕采集状态
            .setSmallIcon(R.drawable.ic_launcher_foreground) // ⚠️ 必填项：你需要替换成你自己的应用图标！
            .setOngoing(true) // 标记为常驻通知，用户无法滑动清除
            .build()

        // 兑现对系统的承诺
        startForeground(NOTIFICATION_ID, notification)

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        registerScreenReceiver()
        scheduleNextAlarm(System.currentTimeMillis() + 2500L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent?.action == HeartbeatBus.ACTION_GLOBAL_TICK) {
            dispatchTick()
        }
        return START_STICKY
    }

    private fun dispatchTick() {
        try {
            // 2. 安排下一次唤醒
            val nextTriggerMs = HeartbeatBus.calculateNextTickTime(System.currentTimeMillis())
            scheduleNextAlarm(nextTriggerMs)

        } catch (e: Exception) {
            Log.e(TAG, "Engine Pipeline Error", e)
            scheduleNextAlarm(System.currentTimeMillis() + HeartbeatBus.HEARTBEAT_BASE)
        }
    }

    /**
     * 安排下一次心跳唤醒（彻底告别精准闹钟）
     * 目标：指向小部件广播，亮屏时弹性触发，锁屏时静默挂起
     */
    private fun scheduleNextAlarm(triggerTimeMs: Long) {
        val intent = Intent(this, CurriculumsWidgetProvider::class.java).apply {
            action = HeartbeatBus.ACTION_GLOBAL_TICK
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setWindow(
            AlarmManager.RTC,
            triggerTimeMs,
            5000,
            pendingIntent
        )
    }

    private fun registerScreenReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> lastScreenState = "已锁屏 (OFF)"
                    Intent.ACTION_USER_PRESENT -> {
                        lastScreenState = "点亮 (ON)"
                        // 解锁即刻采样
                        dispatchTick()
                    }
                }
            }
        }
        registerReceiver(screenReceiver, filter)
    }

    override fun onDestroy() {
        isRunning = false
        // 撤销 AlarmManager，注意这里也要改成指向 Widget 的 Intent
        val intent = Intent(this, CurriculumsWidgetProvider::class.java).apply {
            action = HeartbeatBus.ACTION_GLOBAL_TICK
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onDestroy()
    }
}