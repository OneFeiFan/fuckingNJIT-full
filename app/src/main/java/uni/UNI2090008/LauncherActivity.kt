//
// Decompiled by Jadx - 414ms
//
package uni.UNI2090008

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.feifan.fuckingnjit.utils.Manager
import com.umeng.commonsdk.UMConfigure
import io.dcloud.PandoraEntryActivity
import io.dcloud.WebAppActivity
import java.lang.reflect.Method
import java.util.LinkedList
import java.util.Queue


class LauncherActivity : PandoraEntryActivity() {
    // 新增工具函数避免重复代码
//    private fun Context.getDynamicId(resName: String): Int {
//        return resources.getIdentifier(resName, "id", packageName)
//    }

//    override fun onCreateAdSplash(context: Context?) {
//        super.onCreateAdSplash(context)
//        (super.mSplashView as? ViewGroup)?.let { splashView ->
//            object : Thread("SplashAdHandler") {
//                override fun run() {
//                    try {
//                        // 提前处理容器透明化
//                        val containerId = context?.getDynamicId("ad_dcloud_splash_container") ?: 0
//                        val countdownId = context?.getDynamicId("ad_dcloud_main_skip") ?: 0.also {
//                            println("未找到 ad_dcloud_main_skip 的 ID")
//                        }
//
//                        // 主线程预处理容器
//                        runOnUiThread {
//                            splashView.findViewById<View>(containerId)?.alpha = 0f
//                        }
//
//                        // 分层查找优化
//                        repeat(50) {
//                            findSkipButton(splashView, countdownId)?.let { button ->
//                                runOnUiThread {
//                                    button.performClick()
//                                    interrupt() // 成功后终止线程
//                                }
//                                return@run
//                            }
//                            sleep(100)
//                        }
//                    } catch (e: InterruptedException) {
//                        // 正常退出处理
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//            }.apply {
//                // 添加线程回收处理
//                Thread.UncaughtExceptionHandler { _, e -> e.printStackTrace() }
//                start()
//            }
//        }
//    }
//
//    // 修改后的查找函数（精简参数）
//    private fun findSkipButton(root: ViewGroup?, targetId: Int): TextView? {
//        if (root == null || targetId == 0) return null
//
//        // 广度优先搜索提升性能
//        val queue: Queue<View> = LinkedList()
//        queue.add(root)
//        while (queue.isNotEmpty()) {
//            val view = queue.poll()
//            when {
//                view.id == targetId && view is TextView -> {
//                    println("找到目标按钮：${view.javaClass.simpleName}")
//                    return view
//                }
//
//                view is ViewGroup -> {
//                    (0 until view.childCount).map { view.getChildAt(it) }.forEach(queue::add)
//                }
//            }
//        }
//        return null
//    }


override fun onCreate(savedInstanceState: Bundle?) {
    val startTime = System.currentTimeMillis()

    UMConfigure.preInit(this, "67d77c8948ac1b4f87e98e5f", "android")
    Manager.init(this@LauncherActivity)
    UMConfigure.init(
        this@LauncherActivity,
        "67d77c8948ac1b4f87e98e5f",
        "android",
        UMConfigure.DEVICE_TYPE_PHONE,
        ""
    )

    val endTime = System.currentTimeMillis()
    Log.d("LaunchTime", "onCreate took ${endTime - startTime} ms")

    super.onCreate(savedInstanceState)
}


//    override fun onPause() {
//        overridePendingTransition(0, 0)
//        super.onPause()
//    }
//
//    private fun a(): Boolean {
//        var method: Method? = null
//        var booleanValue: Boolean = false
//        var z = false
//        try {
//            val obtainStyledAttributes = obtainStyledAttributes(
//                (Class.forName("com.android.internal.R\$styleable")
//                    .getField("Window")[null] as IntArray)
//            )
//            method = ActivityInfo::class.java.getMethod(
//                "isTranslucentOrFloating",
//                TypedArray::class.java
//            )
//            method.isAccessible = true
//            booleanValue = (method.invoke(null, obtainStyledAttributes) as Boolean)
//        } catch (e: Exception) {
//        }
//        try {
//            method?.isAccessible = false
//            return booleanValue
//        } catch (e2: Exception) {
//            z = booleanValue
//            return z
//        }
//    }
//
//    companion object {
//        @SuppressLint("WrongConstant")
//        private fun a(activity: Activity) {
//            try {
//                val declaredField = Activity::class.java.getDeclaredField("mActivityInfo")
//                declaredField.isAccessible = true
//                (declaredField[activity] as ActivityInfo).screenOrientation = -1
//            } catch (e: Exception) {
//            }
//        }
//    }
}