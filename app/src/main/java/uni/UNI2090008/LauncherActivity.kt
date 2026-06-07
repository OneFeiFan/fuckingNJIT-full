package uni.UNI2090008


import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import com.feifan.fuckingnjit.utils.CoreInitializer
import com.feifan.fuckingnjit.widget.CurriculumsWidgetProvider
import com.umeng.commonsdk.UMConfigure
import io.dcloud.PandoraEntryActivity


class LauncherActivity : PandoraEntryActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        UMConfigure.preInit(this, "67d77c8948ac1b4f87e98e5f", "android")
        CoreInitializer.init()
        UMConfigure.init(
            this@LauncherActivity,
            "67d77c8948ac1b4f87e98e5f",
            "android",
            UMConfigure.DEVICE_TYPE_PHONE,
            ""
        )
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, CurriculumsWidgetProvider::class.java)
        val hasWidgets = appWidgetManager.getAppWidgetIds(componentName).isNotEmpty()

        if (hasWidgets) {
            println("1111")
            CurriculumsWidgetProvider.pingEngine(this, force = false)
        }
        super.onCreate(savedInstanceState)
    }
}