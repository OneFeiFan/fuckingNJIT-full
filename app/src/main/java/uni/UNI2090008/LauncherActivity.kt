package uni.UNI2090008


import android.os.Bundle
import com.feifan.fuckingnjit.utils.Manager
import com.umeng.commonsdk.UMConfigure
import io.dcloud.PandoraEntryActivity


class LauncherActivity : PandoraEntryActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        UMConfigure.preInit(this, "67d77c8948ac1b4f87e98e5f", "android")
        Manager.init(this@LauncherActivity)
        UMConfigure.init(
            this@LauncherActivity,
            "67d77c8948ac1b4f87e98e5f",
            "android",
            UMConfigure.DEVICE_TYPE_PHONE,
            ""
        )
        super.onCreate(savedInstanceState)
    }
}