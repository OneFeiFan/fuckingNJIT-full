@file:Suppress("UNCHECKED_CAST", "USELESS_CAST", "INAPPLICABLE_JVM_NAME", "UNUSED_ANONYMOUS_PARAMETER", "NAME_SHADOWING")
package uts.sdk.modules.fuckingNJIT
import com.feifan.fuckingnjit.utils.Manager
import com.feifan.fuckingnjit.utils.wifiauth.PortalManager
import com.feifan.fuckingnjit.widget.CurriculumsWidget
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
open class CourseParams (
    @JsonNotNull
    open var course: String,
    open var hideRule: String? = null,
) : UTSObject()
open class DeleteCourse (
    @JsonNotNull
    open var courseId: String,
    @JsonNotNull
    open var isSystem: Boolean = false,
    open var day: Int? = null,
    open var start: Int? = null,
) : UTSObject()
open class RestoreCourse (
    @JsonNotNull
    open var courseId: String,
    @JsonNotNull
    open var day: Int = 0,
    @JsonNotNull
    open var start: Int = 0,
) : UTSObject()
fun parseUTSResponse(data: String): Any {
    try {
        val result = JSON.parseObject(data)
        if (result === null) {
            throw UTSError("数据解析失败：JSON格式可能不正确或数据为空")
        }
        return result
    }
     catch (e: Throwable) {
        return e
    }
}
open class Core {
    public open fun showToast(str: String) {
        Manager.showToast(str)
    }
    public open fun startLogin(relogin: Boolean) {
        Manager.startLogin(relogin)
    }
    public open fun getCurriculum(refresh: Boolean): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
                return@w JSON.parse<UTSJSONObject>(await(Manager.getUserManager().getCurriculum(refresh))) ?: UTSJSONObject()
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
    public open fun getSorces(xnm: String, xqm: String, refresh: Boolean): UTSPromise<String> {
        return wrapUTSPromise(suspend w@{
                return@w await(Manager.getUserManager().getUserScores(xnm, xqm, refresh))
        })
    }
    public open fun getSorcesDetail(classId: String, schoolYear: String, semester: String, courseName: String): UTSPromise<Any> {
        return wrapUTSPromise(suspend w@{
                val data = await(Manager.getWebService().getSorcesDetail(classId, schoolYear, semester, courseName).toJSONString())
                return@w parseUTSResponse(data)
        })
    }
    public open fun getNoticeInformation(): UTSPromise<Any> {
        return wrapUTSPromise(suspend w@{
                val data = await(await(Manager.getWebService().getNoticeInformation().toJSONString()))
                return@w parseUTSResponse(data)
        })
    }
    public open fun getAcademicProgress(refresh: Boolean): UTSPromise<Any> {
        return wrapUTSPromise(suspend w@{
                val data = await(await(Manager.getUserManager().getAcademicProgress(refresh).toJSONString()))
                return@w parseUTSResponse(data)
        })
    }
    public open fun saveCourse(data: CourseParams): UTSJSONObject {
        return JSON.parse<UTSJSONObject>(Manager.getWebService().saveCourse(data.course, data.hideRule).toJSONString()) ?: UTSJSONObject()
    }
    public open fun deleteCourse(data: DeleteCourse): UTSJSONObject {
        return JSON.parse<UTSJSONObject>(Manager.getWebService().deleteCourse(data.courseId, data.isSystem, data.day, data.start).toJSONString()) ?: UTSJSONObject()
    }
    public open fun restoreCourse(data: RestoreCourse): UTSJSONObject {
        return JSON.parse<UTSJSONObject>(Manager.getWebService().restoreCourse(data.courseId, data.day, data.start).toJSONString()) ?: UTSJSONObject()
    }
    public open fun getDate(): String {
        return Manager.getWebService().getDate()
    }
    public open fun createWidget(): String {
        return CurriculumsWidget(UTSAndroid.getAppContext()!!).createWidget()
    }
    public open fun getWidgetPermission() {
        return CurriculumsWidget(UTSAndroid.getAppContext()!!).getPermission()
    }
    public open fun isWidgetAlreadyCreated(): Boolean {
        return CurriculumsWidget(UTSAndroid.getAppContext()!!).isWidgetAlreadyCreated()
    }
    public open fun switchStatus(status: Boolean): Unit {
        PortalManager.switchStatus(UTSAndroid.getAppContext()!!, status)
    }
    public open fun isEnabled(): Boolean {
        return PortalManager.isEnabled(UTSAndroid.getAppContext()!!)
    }
    public open fun setWifiAuthTupe(type: String) {
        Manager.setWifiAuthTupe(type)
    }
    public open fun getWifiAuthTupe(): String {
        return Manager.getWifiAuthTupe()
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
    public open fun updateApp(url: String): UTSPromise<Boolean> {
        return wrapUTSPromise(suspend w@{
                return@w await(Manager.updateApp(url))
        })
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
    public open fun initYiBan(mobile: String, password: String): UTSPromise<Any> {
        return wrapUTSPromise(suspend w@{
                val data = await(Manager.initYiBan(mobile, password).toJSONString())
                return@w parseUTSResponse(data)
        })
    }
}
open class CourseParamsJSONObject : UTSJSONObject() {
    open lateinit var course: String
    open var hideRule: String? = null
}
open class DeleteCourseJSONObject : UTSJSONObject() {
    open lateinit var courseId: String
    open var isSystem: Boolean = false
    open var day: Int? = null
    open var start: Int? = null
}
open class RestoreCourseJSONObject : UTSJSONObject() {
    open lateinit var courseId: String
    open var day: Int = 0
    open var start: Int = 0
}
open class CoreByJs : Core {
    constructor() : super() {}
    public open fun showToastByJs(str: String) {
        return this.showToast(str)
    }
    public open fun startLoginByJs(relogin: Boolean) {
        return this.startLogin(relogin)
    }
    public open suspend fun getCurriculumByJs(refresh: Boolean): Deferred<UTSJSONObject> {
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
    public open suspend fun getSorcesByJs(xnm: String, xqm: String, refresh: Boolean): Deferred<String> {
        return toDeferred(this.getSorces(xnm, xqm, refresh))
    }
    public open suspend fun getSorcesDetailByJs(classId: String, schoolYear: String, semester: String, courseName: String): Deferred<Any> {
        return toDeferred(this.getSorcesDetail(classId, schoolYear, semester, courseName))
    }
    public open suspend fun getNoticeInformationByJs(): Deferred<Any> {
        return toDeferred(this.getNoticeInformation())
    }
    public open suspend fun getAcademicProgressByJs(refresh: Boolean): Deferred<Any> {
        return toDeferred(this.getAcademicProgress(refresh))
    }
    public open fun saveCourseByJs(data: CourseParamsJSONObject): UTSJSONObject {
        return this.saveCourse(CourseParams(course = data.course, hideRule = data.hideRule))
    }
    public open fun deleteCourseByJs(data: DeleteCourseJSONObject): UTSJSONObject {
        return this.deleteCourse(DeleteCourse(courseId = data.courseId, isSystem = data.isSystem, day = data.day, start = data.start))
    }
    public open fun restoreCourseByJs(data: RestoreCourseJSONObject): UTSJSONObject {
        return this.restoreCourse(RestoreCourse(courseId = data.courseId, day = data.day, start = data.start))
    }
    public open fun getDateByJs(): String {
        return this.getDate()
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
    public open fun switchStatusByJs(status: Boolean): Unit {
        return this.switchStatus(status)
    }
    public open fun isEnabledByJs(): Boolean {
        return this.isEnabled()
    }
    public open fun setWifiAuthTupeByJs(type: String) {
        return this.setWifiAuthTupe(type)
    }
    public open fun getWifiAuthTupeByJs(): String {
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
    public open suspend fun updateAppByJs(url: String): Deferred<Boolean> {
        return toDeferred(this.updateApp(url))
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
    public open suspend fun initYiBanByJs(mobile: String, password: String): Deferred<Any> {
        return toDeferred(this.initYiBan(mobile, password))
    }
}
