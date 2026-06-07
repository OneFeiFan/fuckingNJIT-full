package com.feifan.fuckingnjit.utils.database

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.feifan.fuckingnjit.utils.academic.TestCourseGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 采用了安卓程序的特殊初始化对象，在程序启动早期完成初始化
class CoreInitProvider : Initializer<Unit> {
    override fun create(context: Context) {
        // 在这里执行初始化操作
        AppDataCenter.init(context)
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                // 这个方法内部包含了 AppDataCenter.saveUser 和 rsaEncrypt
//                TestCourseGenerator.generateAndSave()
//                Log.i("Init", "后台测试数据生成完毕")
//            } catch (e: Exception) {
//                Log.e("Init", "数据生成失败", e)
//            }
//        }
        // 用于清空数据库的例子
        AppDataCenter.getBoxStore()?.let { store ->
            DbClearHelper.checkAndClear(context, store, "core_db_v1.0.1")
        }
    }

    // 这个方法用于声明依赖关系，确保在 xxx初始化之后初再始化CoreInitProvider
    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList() // 本软件没有这种依赖关系
    }
}