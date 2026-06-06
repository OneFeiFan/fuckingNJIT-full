package com.feifan.fuckingnjit.decision

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Suppress("unused")
object AlarmHelper {
    private const val AUTO_ALARM_PREFIX = "[FNJIT-AUTO]"

    /**
     * 供前台 Vue UI 点击“立即确认闹钟”时使用
     * 保留弹窗 UI，让用户有明确的操作反馈
     */
    fun setSystemAlarm(context: Context, alarmInfo: AlarmInfo): Boolean {
        if (!alarmInfo.canSetAlarm) return false
        try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, alarmInfo.suggestedWakeUpHour)
                putExtra(AlarmClock.EXTRA_MINUTES, alarmInfo.suggestedWakeUpMinute)
                putExtra(AlarmClock.EXTRA_MESSAGE, alarmInfo.alarmLabel)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false) // 显式要求系统弹窗确认
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * 供后台主心跳或静默刷新调用
     *
     * @param context Context
     * @param alarmInfo 决策引擎计算出的最新闹钟信息
     * @param lastSyncedLabel 前端缓存记录的"上一次成功设置的闹钟标签"
     * @return 成功设置的新闹钟标签（如果未变更则返回 null，由 UTS 层判断处理）
     */
    fun autoSyncAlarm(context: Context, alarmInfo: AlarmInfo, lastSyncedLabel: String?): String? {
        if (!alarmInfo.canSetAlarm) return null

        val hour = alarmInfo.suggestedWakeUpHour.coerceIn(0, 23)
        val minute = alarmInfo.suggestedWakeUpMinute.coerceIn(0, 59)

        val timeStr = String.format(Locale.ROOT, "%02d:%02d", hour, minute)
        // 获取明天的日期，拼接到标签中
        val tomorrow = LocalDate.now().plusDays(1)
        val dateStr = tomorrow.format(DateTimeFormatter.ofPattern("MM-dd"))
        val newLabel = "$AUTO_ALARM_PREFIX-$dateStr 明早 $timeStr 起床"

        if (newLabel == lastSyncedLabel) {
            return lastSyncedLabel
        }

        try {
            val setIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, newLabel)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)

                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(setIntent)
            Log.d("AlarmHelper", "已优先发送静默创建新闹钟指令: $newLabel")

            Thread.sleep(2500)

            if (!lastSyncedLabel.isNullOrEmpty()) {
                val dismissIntent = Intent(AlarmClock.ACTION_DISMISS_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_LABEL)
                    putExtra(AlarmClock.EXTRA_MESSAGE, lastSyncedLabel)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dismissIntent)
                Log.d("AlarmHelper", "已发送关闭旧闹钟指令: $lastSyncedLabel")
            }

            return newLabel

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}