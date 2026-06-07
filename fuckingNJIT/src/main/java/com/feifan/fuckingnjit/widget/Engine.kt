package com.feifan.fuckingnjit.widget

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.feifan.fuckingnjit.service.CoreService
import com.feifan.fuckingnjit.utils.HeartbeatBus

@Keep
object Engine {
    @JvmStatic
    @Keep
    fun pingEngine(context: Context, force: Boolean) {
        if (!force && CoreService.isRunning) {
            return
        }

        val serviceIntent = Intent(context, CoreService::class.java).apply {
            action = HeartbeatBus.ACTION_GLOBAL_TICK
        }

        try {
            context.startForegroundService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}