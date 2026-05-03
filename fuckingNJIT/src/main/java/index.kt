@file:Suppress("UNCHECKED_CAST", "USELESS_CAST", "INAPPLICABLE_JVM_NAME", "UNUSED_ANONYMOUS_PARAMETER", "NAME_SHADOWING", "UNNECESSARY_NOT_NULL_ASSERTION")
package uts.sdk.modules.fuckingNJIT
import com.feifan.fuckingnjit.decision.DecisionFacade
import com.feifan.fuckingnjit.service.DataSyncService
import com.feifan.fuckingnjit.utils.AppConfig
import com.feifan.fuckingnjit.utils.CoreInitializer
import com.feifan.fuckingnjit.utils.Manager
import com.feifan.fuckingnjit.utils.network.wifiauth.PortalManager
import com.feifan.fuckingnjit.utils.system.SystemActionHelper
import com.feifan.fuckingnjit.widget.CurriculumsWidget
import io.dcloud.uniapp.*
import io.dcloud.uniapp.extapi.*
import io.dcloud.uts.*
import io.dcloud.uts.Map
import io.dcloud.uts.Set
import io.dcloud.uts.UTSAndroid
import kotlin.properties.Delegates
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
typealias AppMode = String
typealias InsightLevel = String
open class DashboardFactors (
    @JsonNotNull
    open var courseStress: Number,
    @JsonNotNull
    open var physicalState: Number,
    @JsonNotNull
    open var focusCost: Number,
) : UTSObject()
open class DashboardInsight (
    @JsonNotNull
    open var show: Boolean = false,
    @JsonNotNull
    open var level: InsightLevel,
    @JsonNotNull
    open var title: String,
    @JsonNotNull
    open var message: String,
) : UTSObject()
open class TimelineCourse (
    @JsonNotNull
    open var time: String,
    @JsonNotNull
    open var name: String,
) : UTSObject()
open class DashboardTimeline (
    @JsonNotNull
    open var targetSleepTime: String,
    @JsonNotNull
    open var offset: String,
    @JsonNotNull
    open var courses: UTSArray<TimelineCourse>,
) : UTSObject()
open class DashboardRawStats (
    @JsonNotNull
    open var sleepDurationStr: String,
    @JsonNotNull
    open var steps: Number,
    @JsonNotNull
    open var targetSteps: Number,
    @JsonNotNull
    open var focusRate: Number,
    @JsonNotNull
    open var distractionMins: Number,
) : UTSObject()
open class DashboardInsightResponse (
    @JsonNotNull
    open var currentMode: AppMode,
    @JsonNotNull
    open var overallScore: Number,
    @JsonNotNull
    open var factors: DashboardFactors,
    @JsonNotNull
    open var actionableInsight: DashboardInsight,
    @JsonNotNull
    open var timeline: DashboardTimeline,
    @JsonNotNull
    open var rawStats: DashboardRawStats,
) : UTSObject()
fun parseUTSResponse(data: String): UTSJSONObject {
    try {
        val result = JSON.parse(data)
        if (result === null) {
            throw UTSError("数据解析失败：JSON格式可能不正确或数据为空")
        }
        return result as UTSJSONObject
    }
     catch (e: UTSError) {
        console.log(e)
        return UTSJSONObject()
    }
}
open class Core {
    public open fun showToast(str: String) {
        SystemActionHelper.showToast(UTSAndroid.getAppContext()!!, str)
    }
    public open fun startLogin(relogin: Boolean) {
        SystemActionHelper.startLogin(UTSAndroid.getAppContext()!!, relogin)
    }
    public open fun getCurriculum(refresh: Boolean): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
                val data = await(Manager.getUserManager().getCurriculum(UTSAndroid.getAppContext()!!, refresh).toJSONString())
                val tmp = parseUTSResponse(data)
                if (tmp.contains("code") && tmp["code"] == 200.0) {
                    return@w tmp["data"]
                } else {
                    this.showToast("课表获取失败")
                    return@w UTSJSONObject()
                }
        })
    }
    public open fun getAllUsers(): String {
        return Manager.getUserManager().getAllUsers(UTSAndroid.getAppContext()!!)
    }
    public open fun setCurrentUser(id: String) {
        return Manager.getUserManager().setCurrentUser(UTSAndroid.getAppContext()!!, id)
    }
    public open fun deleteUser(id: String): UTSPromise<Boolean> {
        return wrapUTSPromise(suspend w@{
                return@w await(Manager.getUserManager().deleteUser(UTSAndroid.getAppContext()!!, id))
        })
    }
    public open fun getEmptyClassrooms(dateRange: String, coursePeriod: String, buildingId: String): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
                val data = await(Manager.getWebService().getEmptyClassrooms(UTSAndroid.getAppContext()!!, dateRange, coursePeriod, buildingId).toJSONString())
                val tmp = parseUTSResponse(data)
                if (tmp.contains("code") && tmp["code"] == 200.0) {
                    return@w tmp["data"]
                } else {
                    this.showToast("空教室获取失败")
                    return@w UTSJSONObject()
                }
        })
    }
    public open fun getSorces(xnm: String, xqm: String, refresh: Boolean): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
                val data = await(Manager.getUserManager().getUserScores(UTSAndroid.getAppContext()!!, xnm, xqm, refresh).toJSONString())
                val tmp = parseUTSResponse(data)
                if (tmp.contains("code") && tmp["code"] == 200.0) {
                    return@w tmp.toJSONString()
                } else {
                    this.showToast("成绩获取失败")
                    return@w UTSJSONObject()
                }
        })
    }
    public open fun getSorcesDetail(classId: String, schoolYear: String, semester: String, courseName: String): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
                val data = await(Manager.getWebService().getSorcesDetail(UTSAndroid.getAppContext()!!, classId, schoolYear, semester, courseName).toJSONString())
                return@w parseUTSResponse(data)
        })
    }
    public open fun getAcademicProgress(refresh: Boolean): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
                val data = await(Manager.getUserManager().getAcademicProgress(UTSAndroid.getAppContext()!!, refresh).toJSONString())
                return@w parseUTSResponse(data)
        })
    }
    public open fun saveCourse(data: CourseParams): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
                val result = await(Manager.getWebService().saveCourse(UTSAndroid.getAppContext()!!, data.course, data.hideRule).toJSONString())
                return@w parseUTSResponse(result)
        })
    }
    public open fun deleteCourse(data: DeleteCourse): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
                val result = await(Manager.getWebService().deleteCourse(UTSAndroid.getAppContext()!!, data.courseId, data.isSystem, data.day, data.start).toJSONString())
                return@w parseUTSResponse(result)
        })
    }
    public open fun restoreCourse(data: RestoreCourse): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
                val result = await(Manager.getWebService().restoreCourse(UTSAndroid.getAppContext()!!, data.courseId, data.day, data.start).toJSONString())
                return@w parseUTSResponse(result)
        })
    }
    public open fun getDate(): String {
        return Manager.getWebService().getDate(UTSAndroid.getAppContext()!!)
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
    public open fun setWifiAuthType(type: String) {
        AppConfig.setWifiAuthType(type)
    }
    public open fun getWifiAuthType(): String {
        return AppConfig.getWifiAuthType()
    }
    public open fun goHome() {
        SystemActionHelper.goHome(UTSAndroid.getAppContext()!!)
    }
    public open fun setPasswordStorageEnabled(enable: Boolean) {
        Manager.getUserManager().setPasswordStorageEnabled(enable)
    }
    public open fun isPasswordStorageEnabled(): Boolean {
        return Manager.getUserManager().isPasswordStorageEnabled()
    }
    public open fun updateApp(url: String): UTSPromise<Boolean> {
        return wrapUTSPromise(suspend w@{
                return@w await(SystemActionHelper.updateApp(UTSAndroid.getAppContext()!!, url))
        })
    }
    public open fun checkInstallPackagePermission(): Boolean {
        return Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).checkRequestInstallPackage()
    }
    public open fun requestInstallPackage(): UTSPromise<Boolean> {
        return UTSPromise(fun(resolve, reject){
            Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).requestRequestInstallPackage(fun(isGranted: Boolean){
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
        return Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).isSmartUpdate()
    }
    public open fun setSmartUpdate(isSmart: Boolean) {
        Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).setSmartUpdate(isSmart)
    }
    public open fun checkRecordAudio(): Boolean {
        return Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).checkRecordAudio()
    }
    public open fun requestRecordAudio(): UTSPromise<Boolean> {
        return UTSPromise(fun(resolve, reject){
            Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).requestRecordAudio(fun(isGranted: Boolean){
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
    public open fun checkNotification(): Boolean {
        return Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).checkNotification()
    }
    public open fun requestNotificationService(): UTSPromise<Boolean> {
        return UTSPromise(fun(resolve, reject){
            Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).requestNotificationServicePermission(fun(isGranted: Boolean){
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
    public open fun checkScheduleExactAlarm(): Boolean {
        return Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).checkScheduleExactAlarm()
    }
    public open fun requestExactAlarm(): UTSPromise<Boolean> {
        return UTSPromise(fun(resolve, reject){
            Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).requestScheduleExactAlarm(fun(isGranted: Boolean){
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
    public open fun requestKeepAliveNormalPermissions(): UTSPromise<UTSArray<String>> {
        return UTSPromise(fun(resolve, reject){
            Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).requestKeepAliveNormalPermissions(fun(isGranted: Boolean, list: List<String>){
                if (isGranted) {
                    resolve(UTSArray<String>())
                } else {
                    reject(UTSArray.fromNative(list))
                }
            }
            )
        }
        )
    }
    public open fun isIgnoringBatteryOptimizations(): Boolean {
        return Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).isIgnoringBatteryOptimizations()
    }
    public open fun requestIgnoreBatteryOptimizations() {
        Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).requestIgnoreBatteryOptimizations()
    }
    public open fun isAccessibilitySettingsOn(): Boolean {
        return Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).isAccessibilitySettingsOn()
    }
    public open fun requestAccessibilityPermission() {
        Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).requestAccessibilityPermission()
    }
    public open fun initYiBan(mobile: String, password: String): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
                val data = await(CoreInitializer.initYiBan(UTSAndroid.getAppContext()!!, mobile, password).toJSONString())
                return@w parseUTSResponse(data)
        })
    }
    public open fun refreshSleepData(): UTSPromise<Unit> {
        return wrapUTSPromise(suspend {
                await(DataSyncService.uploadAndClearData())
        })
    }
    public open fun switchAppMode(mode: String) {
        DecisionFacade.switchAppMode(mode)
    }
    public open fun getDashboardInsight(): UTSPromise<DashboardInsightResponse> {
        return wrapUTSPromise(suspend w@{
                val data = await(DecisionFacade.getDashboardInsight(UTSAndroid.getAppContext()!!).toJSONString())
                val tmp = parseUTSResponse(data)
                if (tmp.contains("code") && tmp["code"] == 200.0) {
                    return@w tmp["data"]
                } else {
                    this.showToast("健康数据获取失败")
                    return@w UTSJSONObject()
                }
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
    public open suspend fun getEmptyClassroomsByJs(dateRange: String, coursePeriod: String, buildingId: String): Deferred<UTSJSONObject> {
        return toDeferred(this.getEmptyClassrooms(dateRange, coursePeriod, buildingId))
    }
    public open suspend fun getSorcesByJs(xnm: String, xqm: String, refresh: Boolean): Deferred<UTSJSONObject> {
        return toDeferred(this.getSorces(xnm, xqm, refresh))
    }
    public open suspend fun getSorcesDetailByJs(classId: String, schoolYear: String, semester: String, courseName: String): Deferred<UTSJSONObject> {
        return toDeferred(this.getSorcesDetail(classId, schoolYear, semester, courseName))
    }
    public open suspend fun getAcademicProgressByJs(refresh: Boolean): Deferred<UTSJSONObject> {
        return toDeferred(this.getAcademicProgress(refresh))
    }
    public open suspend fun saveCourseByJs(data: CourseParamsJSONObject): Deferred<UTSJSONObject> {
        return toDeferred(this.saveCourse(CourseParams(course = data.course, hideRule = data.hideRule)))
    }
    public open suspend fun deleteCourseByJs(data: DeleteCourseJSONObject): Deferred<UTSJSONObject> {
        return toDeferred(this.deleteCourse(DeleteCourse(courseId = data.courseId, isSystem = data.isSystem, day = data.day, start = data.start)))
    }
    public open suspend fun restoreCourseByJs(data: RestoreCourseJSONObject): Deferred<UTSJSONObject> {
        return toDeferred(this.restoreCourse(RestoreCourse(courseId = data.courseId, day = data.day, start = data.start)))
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
    public open fun setWifiAuthTypeByJs(type: String) {
        return this.setWifiAuthType(type)
    }
    public open fun getWifiAuthTypeByJs(): String {
        return this.getWifiAuthType()
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
    public open fun checkInstallPackagePermissionByJs(): Boolean {
        return this.checkInstallPackagePermission()
    }
    public open suspend fun requestInstallPackageByJs(): Deferred<Boolean> {
        return toDeferred(this.requestInstallPackage())
    }
    public open fun isSmartUpdateByJs(): Boolean {
        return this.isSmartUpdate()
    }
    public open fun setSmartUpdateByJs(isSmart: Boolean) {
        return this.setSmartUpdate(isSmart)
    }
    public open fun checkRecordAudioByJs(): Boolean {
        return this.checkRecordAudio()
    }
    public open suspend fun requestRecordAudioByJs(): Deferred<Boolean> {
        return toDeferred(this.requestRecordAudio())
    }
    public open fun checkNotificationByJs(): Boolean {
        return this.checkNotification()
    }
    public open suspend fun requestNotificationServiceByJs(): Deferred<Boolean> {
        return toDeferred(this.requestNotificationService())
    }
    public open fun checkScheduleExactAlarmByJs(): Boolean {
        return this.checkScheduleExactAlarm()
    }
    public open suspend fun requestExactAlarmByJs(): Deferred<Boolean> {
        return toDeferred(this.requestExactAlarm())
    }
    public open suspend fun requestKeepAliveNormalPermissionsByJs(): Deferred<UTSArray<String>> {
        return toDeferred(this.requestKeepAliveNormalPermissions())
    }
    public open fun isIgnoringBatteryOptimizationsByJs(): Boolean {
        return this.isIgnoringBatteryOptimizations()
    }
    public open fun requestIgnoreBatteryOptimizationsByJs() {
        return this.requestIgnoreBatteryOptimizations()
    }
    public open fun isAccessibilitySettingsOnByJs(): Boolean {
        return this.isAccessibilitySettingsOn()
    }
    public open fun requestAccessibilityPermissionByJs() {
        return this.requestAccessibilityPermission()
    }
    public open suspend fun initYiBanByJs(mobile: String, password: String): Deferred<UTSJSONObject> {
        return toDeferred(this.initYiBan(mobile, password))
    }
    public open suspend fun refreshSleepDataByJs(): Deferred<Unit> {
        return toDeferred(this.refreshSleepData())
    }
    public open fun switchAppModeByJs(mode: String) {
        return this.switchAppMode(mode)
    }
    public open suspend fun getDashboardInsightByJs(): Deferred<DashboardInsightResponse> {
        return toDeferred(this.getDashboardInsight())
    }
}
