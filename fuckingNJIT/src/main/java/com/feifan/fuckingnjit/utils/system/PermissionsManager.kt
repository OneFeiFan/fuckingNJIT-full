package com.feifan.fuckingnjit.utils.system

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.feifan.fuckingnjit.service.CoreService
import com.feifan.fuckingnjit.utils.database.AppDataCenter
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission


@Suppress("unused")
class PermissionsManager private constructor(private var context: Context) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: PermissionsManager? = null

        fun getInstance(context: Context): PermissionsManager {
            return instance?.apply {
                this.context = context // 更新上下文对象
            } ?: synchronized(this) {
                instance?.apply {
                    this.context = context
                } ?: PermissionsManager(context).also { instance = it }
            }
        }
    }

    // 智能更新相关
    fun isSmartUpdate(): Boolean {
        return AppDataCenter.getSystemConfig().smartUpdate
    }

    fun setSmartUpdate(isSmart: Boolean) {
        AppDataCenter.updateSystemConfig { it.smartUpdate = isSmart }
    }

    // 检查和业务强相关的权限
    fun checkKeepAliveNormalPermissions(): Boolean {
        val requestList = getKeepAlivePermissionList()

        // 获取被拒绝的权限列表
        val deniedList = XXPermissions.getDeniedPermissions(context, requestList)

        // 如果被拒绝的列表为空（或者为 null），说明所有请求的权限都已经授予了
        return deniedList == null || deniedList.isEmpty()
    }

    // 申请和业务相关的基础权限
    fun requestKeepAliveNormalPermissions(callback: (Boolean, List<String>) -> Unit) {
        XXPermissions.with(context)
            .permissions(getKeepAlivePermissionList())
            .request { grantedList, deniedList ->
                val allGranted = deniedList.isEmpty()
                if (allGranted) {
                    // 如果全部权限都有了就启动监测进程（其实不应该在这里启动，但是懒得安排在其它地方了
                    val intent = Intent(context, CoreService::class.java)
                    context.startForegroundService(intent)
                }
                // 将 IPermission 转为 String 列表返回给前端，方便前端判断哪个被拒了
                val deniedStrList = deniedList.map { it.toString() }
                callback(allGranted, deniedStrList)
            }
    }

    private fun getKeepAlivePermissionList(): List<IPermission> {
        return listOf(
            PermissionLists.getScheduleExactAlarmPermission(),// 精确闹钟权限
        )
    }

    // 麦克风权限
    fun checkRecordAudio(): Boolean =
        XXPermissions.isGrantedPermission(context, PermissionLists.getRecordAudioPermission())

    fun requestRecordAudio(callback: (Boolean) -> Unit) =
        requestSinglePermission(PermissionLists.getRecordAudioPermission(), callback)

    // 安装未知应用权限
    fun checkRequestInstallPackage(): Boolean =
        XXPermissions.isGrantedPermission(
            context,
            PermissionLists.getRequestInstallPackagesPermission()
        )

    fun requestRequestInstallPackage(callback: (Boolean) -> Unit) =
        requestSinglePermission(PermissionLists.getRequestInstallPackagesPermission(), callback)


    private fun requestSinglePermission(permission: IPermission, callback: (Boolean) -> Unit) {
        XXPermissions.with(context)
            .permission(permission)
            .request { grantedList, deniedList -> // 只要没有被拒绝的，就认为是成功
                callback(deniedList.isEmpty())
            }
    }
}