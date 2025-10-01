//package uni.UNI2090008
//
//import android.app.Activity
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import android.view.View
//import android.view.ViewGroup
//import android.widget.FrameLayout
//import android.widget.TextView
//import com.umeng.union.UMSplashAD
//import com.umeng.union.UMUnionSdk
//import com.umeng.union.api.UMAdConfig
//import com.umeng.union.api.UMUnionApi.AdLoadListener
//import com.umeng.union.api.UMUnionApi.AdType
//import com.umeng.union.api.UMUnionApi.SplashAdListener
//
//
//class UMSplashAdDemo : Activity() {
//    private val layoutParams = FrameLayout.LayoutParams(
//        1, // 宽度
//        1 // 高度（像素值）
//    )
//
//    //请求回调
//    private val mLoadListener: AdLoadListener<UMSplashAD> = object : AdLoadListener<UMSplashAD> {
//        override fun onSuccess(type: AdType, display: UMSplashAD) {
//            log("请求广告成功")
//            val runnable = mReqTimeout
//            if (runnable != null) {
//                mHandler.removeCallbacks(runnable)
//            }
//            mReqTimeout = null
//            if (isFinishing) {
//                return
//            }
////            container?.alpha = 0.0f // 范围 0.0（完全透明）~1.0（完全不透明）
//
//            display.adEventListener = object : SplashAdListener {
//                override fun onDismissed() {
//                    log("onAdDismissed()")
//                    goHome()
//                }
//
//                override fun onExposed() {
//                    log("onAdExposed()")
////                    container?.layoutParams = layoutParams
////                    Handler(Looper.getMainLooper()).postDelayed({
////                        // 递归查找跳过按钮（假设它的文本包含"跳过"）
////                        val skipButton = findSkipButton(container)
////                        skipButton?.performClick()
////                    }, (100..500).random().toLong()) // 延迟 500ms
//                }
//
//                override fun onClicked(view: View) {
//                    log("onAdClicked()")
//                }
//
//                override fun onError(code: Int, message: String) {
//                    log("onError() code:$code msg:$message")
//                }
//            }
////            display.disableCountdown()
//            display.show(container)
//        }
//
//        override fun onFailure(type: AdType, message: String) {
//            log(message)
//            val runnable = mReqTimeout
//            if (runnable != null) {
//                mHandler.removeCallbacks(runnable)
//            }
//            mReqTimeout = null
//            if (isFinishing) {
//                return
//            }
//            goHome()
//        }
//    }
//
//    private var container: ViewGroup? = null
//
//    private var canJump = false
//
//    private var mReqTimeout: Runnable? = object : Runnable {
//        override fun run() {
//            mReqTimeout = null
//            goHome()
//        }
//    }
//
//    private val mHandler: Handler = Handler(Looper.getMainLooper())
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        container = FrameLayout(this)
//        setContentView(container)
//        val config = UMAdConfig.Builder().setSlotId("100002955").build()
//        UMUnionSdk.loadSplashAd(config, mLoadListener, 3000)
//        mReqTimeout?.let { mHandler.postDelayed(it, 3000) }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        if (mReqTimeout != null) {
//            mHandler.removeCallbacks(mReqTimeout!!)
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (canJump) {
//            goHome()
//        }
//        canJump = true
//    }
//
//    private fun findSkipButton(viewGroup: ViewGroup?): TextView? {
//        if (viewGroup == null) return null
//
//        // 动态获取 umeng_splash_countdown_tv 的 ID
//        val countdownId = viewGroup.resources.getIdentifier(
//            "umeng_splash_countdown_tv",  // 资源名称
//            "id",                         // 资源类型（id）
//            viewGroup.context.packageName // 包名（通常是当前应用的包名）
//        )
//
//        if (countdownId == 0) {
//            println("未找到 umeng_splash_countdown_tv 的 ID")
//            return null
//        }
//
//        for (i in 0 until viewGroup.childCount) {
//            val child = viewGroup.getChildAt(i)
//
//            // 检查当前子视图是否是目标 TextView
//            if (child.id == countdownId && child is TextView) {
//                println("找到倒计时 TextView：$child")
//                return child
//            }
//
//            // 如果是 ViewGroup，递归查找
//            if (child is ViewGroup) {
//                val result = findSkipButton(child)
//                if (result != null) return result
//            }
//        }
//        return null
//    }
//
//
////    override fun onPause() {
////        super.onPause()
//////        canJump = false
////    }
//
//    private fun goHome() {
//        log("goHome $canJump")
//        if (canJump) {
////            startActivity(Intent(this, DemoMainActivity::class.java))
//            finish()
//        } else {
//            canJump = true
//        }
//    }
//
////    override fun onBackPressed() {
////    }
//
//    private fun log(msg: String) {
//        Log.d("Splash__", msg)
//    }
//}