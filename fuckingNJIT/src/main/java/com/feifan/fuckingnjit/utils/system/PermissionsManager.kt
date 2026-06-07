package com.feifan.fuckingnjit.utils.system

import android.annotation.SuppressLint
import android.content.Context
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