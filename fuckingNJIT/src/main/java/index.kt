@file:Suppress("UNCHECKED_CAST", "USELESS_CAST", "INAPPLICABLE_JVM_NAME", "UNUSED_ANONYMOUS_PARAMETER", "NAME_SHADOWING")
package uts.sdk.modules.fuckingNJIT
import com.feifan.fuckingnjit.utils.Manager
import com.feifan.fuckingnjit.widget.DemoMainWidget
import com.feifan.fuckingnjit.widget.WifiAuth
import io.dcloud.uniapp.*
import io.dcloud.uniapp.extapi.*
import io.dcloud.uts.*
import io.dcloud.uts.Map
import io.dcloud.uts.Set
import io.dcloud.uts.UTSAndroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
open class Core {
    public open fun showToast(str: String) {
        Manager.showToast(str)
    }
    public open fun startLogin(relogin: Boolean) {
        Manager.startLogin(relogin)
    }
    public open fun getCurriculum(refresh: Boolean): UTSPromise<String> {
        return wrapUTSPromise(suspend w@{
                return@w Manager.getUserManager().getCurriculum(refresh)
        })
    }
    public open fun getAllUsers(): String {
        return Manager.getUserManager().getAllUsers()
    }
    public open fun setCurrentUser(id: String) {
        return Manager.getUserManager().setCurrentUser(id)
    }
    public open fun deleteUser(id: String): UTSPromise<Boolean> {
        return wrapUTSPromise(suspend w@{
                return@w await(Manager.getUserManager().deleteUser(id))
        })
    }
    public open fun getEmptyClassrooms(dateRange: String, coursePeriod: String, buildingId: String): UTSPromise<String> {
        return wrapUTSPromise(suspend w@{
                return@w await(Manager.getWebService().getEmptyClassrooms(dateRange, coursePeriod, buildingId))
        })
    }
    public open fun getAllSorces(refresh: Boolean): UTSPromise<String> {
        return wrapUTSPromise(suspend w@{
                return@w await(Manager.getUserManager().getUserScores(refresh))
        })
    }
    public open fun getSorcesDetail(classId: String, schoolYear: String, semester: String, courseName: String): UTSPromise<String> {
        return wrapUTSPromise(suspend w@{
                return@w await(Manager.getWebService().getSorcesDetail(classId, schoolYear, semester, courseName))
        })
    }
    public open fun getNoticeInformation(): UTSPromise<String> {
        return wrapUTSPromise(suspend w@{
                return@w await(Manager.getWebService().getNoticeInformation())
        })
    }
    public open fun getAcademicProgress(refresh: Boolean): UTSPromise<String> {
        return wrapUTSPromise(suspend w@{
                return@w await(Manager.getWebService().getAcademicProgress(refresh))
        })
    }
    public open fun createWidget(): String {
        return DemoMainWidget(UTSAndroid.getAppContext()!!).createWidget()
    }
    public open fun getWidgetPermission() {
        return DemoMainWidget(UTSAndroid.getAppContext()!!).getPermission()
    }
    public open fun isWidgetAlreadyCreated(): Boolean {
        return DemoMainWidget(UTSAndroid.getAppContext()!!).isWidgetAlreadyCreated()
    }
    public open fun createWifiAuthWidget(): String {
        return WifiAuth(UTSAndroid.getAppContext()!!).createWidget()
    }
    public open fun getWifiAuthWidgetPermission() {
        return WifiAuth(UTSAndroid.getAppContext()!!).getPermission()
    }
    public open fun isWifiAuthWidgetAlreadyCreated(): Boolean {
        return WifiAuth(UTSAndroid.getAppContext()!!).isWidgetAlreadyCreated()
    }
    public open fun setWifiAuthTupe(type: String) {
        Manager.setWifiAuthTupe(type)
    }
    public open fun getWifiAuthTupe() {
        Manager.getWifiAuthTupe()
    }
    public open fun goHome() {
        Manager.goHome()
    }
    public open fun setPasswordStorageEnabled(enable: Boolean) {
        Manager.getUserManager().setPasswordStorageEnabled(enable)
    }
    public open fun isPasswordStorageEnabled(): Boolean {
        return Manager.getUserManager().isPasswordStorageEnabled()
    }
    public open fun getSemesterStartDate(): String {
        return Manager.getSemesterStartDate()
    }
    public open fun updateApp(url: String): UTSPromise<Boolean> {
        return wrapUTSPromise(suspend w@{
                return@w await(Manager.updateApp(url))
        })
    }
    public open fun checkOverlayWindowPermission(): Boolean {
        return Manager.getPermissionsManager().checkOverlayWindowPermission()
    }
    public open fun requestOverlayWindowPermission(): UTSPromise<Boolean> {
        return UTSPromise(fun(resolve, reject){
            Manager.getPermissionsManager().requestOverlayWindowPermission(fun(isGranted: Boolean){
                if (isGranted) {
                    resolve(true)
                } else {
                    reject(false)
                }
            }
            )
        }
        )
    }
    public open fun checkRequestInstallPackagePermission(): Boolean {
        return Manager.getPermissionsManager().checkRequestInstallPackagePermission()
    }
    public open fun requestRequestInstallPackagePermission(): UTSPromise<Boolean> {
        return UTSPromise(fun(resolve, reject){
            Manager.getPermissionsManager().requestRequestInstallPackagePermission(fun(isGranted: Boolean){
                if (isGranted) {
                    resolve(true)
                } else {
                    reject(false)
                }
            }
            )
        }
        )
    }
    public open fun isSmartUpdate(): Boolean {
        return Manager.getPermissionsManager().isSmartUpdate()
    }
    public open fun setSmartUpdate(isSmart: Boolean) {
        Manager.getPermissionsManager().setSmartUpdate(isSmart)
    }
    public open fun setLocalCurriculums(data: String) {
        Manager.setLocalCurriculums(data)
    }
    public open fun modifyLocalCurriculums(keys: String) {
        Manager.modifyLocalCurriculums(keys)
    }
    public open fun reSetLocalCurriculums() {
        Manager.reSetLocalCurriculums()
    }
}
open class CoreByJs : Core {
    constructor() : super() {}
    public open fun showToastByJs(str: String) {
        return this.showToast(str)
    }
    public open fun startLoginByJs(relogin: Boolean) {
        return this.startLogin(relogin)
    }
    public open suspend fun getCurriculumByJs(refresh: Boolean): Deferred<String> {
        return toDeferred(this.getCurriculum(refresh))
    }
    public open fun getAllUsersByJs(): String {
        return this.getAllUsers()
    }
    public open fun setCurrentUserByJs(id: String) {
        return this.setCurrentUser(id)
    }
    public open suspend fun deleteUserByJs(id: String): Deferred<Boolean> {
        return toDeferred(this.deleteUser(id))
    }
    public open suspend fun getEmptyClassroomsByJs(dateRange: String, coursePeriod: String, buildingId: String): Deferred<String> {
        return toDeferred(this.getEmptyClassrooms(dateRange, coursePeriod, buildingId))
    }
    public open suspend fun getAllSorcesByJs(refresh: Boolean): Deferred<String> {
        return toDeferred(this.getAllSorces(refresh))
    }
    public open suspend fun getSorcesDetailByJs(classId: String, schoolYear: String, semester: String, courseName: String): Deferred<String> {
        return toDeferred(this.getSorcesDetail(classId, schoolYear, semester, courseName))
    }
    public open suspend fun getNoticeInformationByJs(): Deferred<String> {
        return toDeferred(this.getNoticeInformation())
    }
    public open suspend fun getAcademicProgressByJs(refresh: Boolean): Deferred<String> {
        return toDeferred(this.getAcademicProgress(refresh))
    }
    public open fun createWidgetByJs(): String {
        return this.createWidget()
    }
    public open fun getWidgetPermissionByJs() {
        return this.getWidgetPermission()
    }
    public open fun isWidgetAlreadyCreatedByJs(): Boolean {
        return this.isWidgetAlreadyCreated()
    }
    public open fun createWifiAuthWidgetByJs(): String {
        return this.createWifiAuthWidget()
    }
    public open fun getWifiAuthWidgetPermissionByJs() {
        return this.getWifiAuthWidgetPermission()
    }
    public open fun isWifiAuthWidgetAlreadyCreatedByJs(): Boolean {
        return this.isWifiAuthWidgetAlreadyCreated()
    }
    public open fun setWifiAuthTupeByJs(type: String) {
        return this.setWifiAuthTupe(type)
    }
    public open fun getWifiAuthTupeByJs() {
        return this.getWifiAuthTupe()
    }
    public open fun goHomeByJs() {
        return this.goHome()
    }
    public open fun setPasswordStorageEnabledByJs(enable: Boolean) {
        return this.setPasswordStorageEnabled(enable)
    }
    public open fun isPasswordStorageEnabledByJs(): Boolean {
        return this.isPasswordStorageEnabled()
    }
    public open fun getSemesterStartDateByJs(): String {
        return this.getSemesterStartDate()
    }
    public open suspend fun updateAppByJs(url: String): Deferred<Boolean> {
        return toDeferred(this.updateApp(url))
    }
    public open fun checkOverlayWindowPermissionByJs(): Boolean {
        return this.checkOverlayWindowPermission()
    }
    public open suspend fun requestOverlayWindowPermissionByJs(): Deferred<Boolean> {
        return toDeferred(this.requestOverlayWindowPermission())
    }
    public open fun checkRequestInstallPackagePermissionByJs(): Boolean {
        return this.checkRequestInstallPackagePermission()
    }
    public open suspend fun requestRequestInstallPackagePermissionByJs(): Deferred<Boolean> {
        return toDeferred(this.requestRequestInstallPackagePermission())
    }
    public open fun isSmartUpdateByJs(): Boolean {
        return this.isSmartUpdate()
    }
    public open fun setSmartUpdateByJs(isSmart: Boolean) {
        return this.setSmartUpdate(isSmart)
    }
    public open fun setLocalCurriculumsByJs(data: String) {
        return this.setLocalCurriculums(data)
    }
    public open fun modifyLocalCurriculumsByJs(keys: String) {
        return this.modifyLocalCurriculums(keys)
    }
    public open fun reSetLocalCurriculumsByJs() {
        return this.reSetLocalCurriculums()
    }
}
