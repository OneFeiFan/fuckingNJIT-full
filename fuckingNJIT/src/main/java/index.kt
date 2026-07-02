@file:Suppress(
    "UNCHECKED_CAST",
    "USELESS_CAST",
    "INAPPLICABLE_JVM_NAME",
    "UNUSED_ANONYMOUS_PARAMETER",
    "NAME_SHADOWING",
    "UNNECESSARY_NOT_NULL_ASSERTION"
)

package uts.sdk.modules.fuckingNJIT

import com.feifan.fuckingnjit.utils.AppConfig
import com.feifan.fuckingnjit.utils.CoreInitializer
import com.feifan.fuckingnjit.utils.Manager
import com.feifan.fuckingnjit.utils.TodayScheduleManager
import com.feifan.fuckingnjit.utils.database.AppDataCenter
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

open class CourseParams(
    @JsonNotNull
    open var course: String,
    open var hideRule: String? = null,
) : UTSObject()

open class DeleteCourse(
    @JsonNotNull
    open var courseId: String,
    @JsonNotNull
    open var isSystem: Boolean = false,
    open var day: Int? = null,
    open var start: Int? = null,
) : UTSObject()

open class RestoreCourse(
    @JsonNotNull
    open var courseId: String,
    @JsonNotNull
    open var day: Int = 0,
    @JsonNotNull
    open var start: Int = 0,
) : UTSObject()

fun parseUTSResponse(data: String): UTSJSONObject {
    try {
        val result = JSON.parse(data)
        if (result === null) {
            throw UTSError("数据解析失败：JSON格式可能不正确或数据为空")
        }
        return result as UTSJSONObject
    } catch (e: UTSError) {
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
            val data = await(
                Manager.getUserManager().getCurriculum(UTSAndroid.getAppContext()!!, refresh)
                    .toJSONString()
            )
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

    public open fun setCustomSemesterStartDate(date: String): Boolean {
        val user = AppDataCenter.getCurrentUser()
        if (user == null) {
            return false
        }
        try {
            if (date.length == 0) {
                user.customSemesterStartDateMs = 0
            } else {
                val parts = date.split("-")
                if (parts.length != 3) {
                    SystemActionHelper.showToast(
                        UTSAndroid.getAppContext()!!,
                        "日期格式错误，请使用 yyyy-MM-dd 格式"
                    )
                    return false
                }
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd")
                val parsedDate = sdf.parse(date)
                user.customSemesterStartDateMs = parsedDate.getTime()
            }
            AppDataCenter.saveUser(user)
            TodayScheduleManager.clearCache()
            return true
        } catch (e: Throwable) {
            SystemActionHelper.showToast(
                UTSAndroid.getAppContext()!!,
                "日期格式错误，请使用 yyyy-MM-dd 格式"
            )
            return false
        }
    }

    public open fun getEmptyClassrooms(
        dateRange: String,
        coursePeriod: String,
        buildingId: String
    ): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
            val data = await(
                Manager.getWebService().getEmptyClassrooms(
                    UTSAndroid.getAppContext()!!,
                    dateRange,
                    coursePeriod,
                    buildingId
                ).toJSONString()
            )
            val tmp = parseUTSResponse(data)
            if (tmp.contains("code") && tmp["code"] == 200.0) {
                return@w tmp["data"]
            } else {
                this.showToast("空教室获取失败")
                return@w UTSJSONObject()
            }
        })
    }

    public open fun getSorces(
        xnm: String,
        xqm: String,
        refresh: Boolean
    ): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
            val data = await(
                Manager.getUserManager()
                    .getUserScores(UTSAndroid.getAppContext()!!, xnm, xqm, refresh).toJSONString()
            )
            val tmp = parseUTSResponse(data)
            if (tmp.contains("code") && tmp["code"] == 200.0) {
                return@w tmp.toJSONString()
            } else {
                this.showToast("成绩获取失败")
                return@w UTSJSONObject()
            }
        })
    }

    public open fun getSorcesDetail(
        classId: String,
        schoolYear: String,
        semester: String,
        courseName: String
    ): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
            val data = await(
                Manager.getWebService().getSorcesDetail(
                    UTSAndroid.getAppContext()!!,
                    classId,
                    schoolYear,
                    semester,
                    courseName
                ).toJSONString()
            )
            return@w parseUTSResponse(data)
        })
    }

    public open fun getAllExam(): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
            val data = await(
                Manager.getWebService().getAllExam(UTSAndroid.getAppContext()!!).toJSONString()
            )
            val tmp = parseUTSResponse(data)
            if (tmp.contains("code") && tmp["code"] == 200.0) {
                return@w tmp["data"]
            } else {
                this.showToast("考试信息获取失败")
                return@w UTSJSONObject()
            }
        })
    }

    public open fun getAcademicProgress(refresh: Boolean): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
            val data = await(
                Manager.getUserManager().getAcademicProgress(UTSAndroid.getAppContext()!!, refresh)
                    .toJSONString()
            )
            return@w parseUTSResponse(data)
        })
    }

    public open fun saveCourse(data: CourseParams): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
            val result = await(
                Manager.getWebService()
                    .saveCourse(UTSAndroid.getAppContext()!!, data.course, data.hideRule)
                    .toJSONString()
            )
            return@w parseUTSResponse(result)
        })
    }

    public open fun deleteCourse(data: DeleteCourse): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
            val result = await(
                Manager.getWebService().deleteCourse(
                    UTSAndroid.getAppContext()!!,
                    data.courseId,
                    data.isSystem,
                    data.day,
                    data.start
                ).toJSONString()
            )
            return@w parseUTSResponse(result)
        })
    }

    public open fun restoreCourse(data: RestoreCourse): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
            val result = await(
                Manager.getWebService().restoreCourse(
                    UTSAndroid.getAppContext()!!,
                    data.courseId,
                    data.day,
                    data.start
                ).toJSONString()
            )
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
        return Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!)
            .checkRequestInstallPackage()
    }

    public open fun requestInstallPackage(): UTSPromise<Boolean> {
        return UTSPromise(fun(resolve, reject) {
            Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!)
                .requestRequestInstallPackage(fun(isGranted: Boolean) {
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

    public open fun initYiBan(mobile: String, password: String): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
            val data = await(
                CoreInitializer.initYiBan(UTSAndroid.getAppContext()!!, mobile, password)
                    .toJSONString()
            )
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

    public open fun setCustomSemesterStartDateByJs(date: String): Boolean {
        return this.setCustomSemesterStartDate(date)
    }

    public open suspend fun getEmptyClassroomsByJs(
        dateRange: String,
        coursePeriod: String,
        buildingId: String
    ): Deferred<UTSJSONObject> {
        return toDeferred(this.getEmptyClassrooms(dateRange, coursePeriod, buildingId))
    }

    public open suspend fun getSorcesByJs(
        xnm: String,
        xqm: String,
        refresh: Boolean
    ): Deferred<UTSJSONObject> {
        return toDeferred(this.getSorces(xnm, xqm, refresh))
    }

    public open suspend fun getSorcesDetailByJs(
        classId: String,
        schoolYear: String,
        semester: String,
        courseName: String
    ): Deferred<UTSJSONObject> {
        return toDeferred(this.getSorcesDetail(classId, schoolYear, semester, courseName))
    }

    public open suspend fun getAllExamByJs(): Deferred<UTSJSONObject> {
        return toDeferred(this.getAllExam())
    }

    public open suspend fun getAcademicProgressByJs(refresh: Boolean): Deferred<UTSJSONObject> {
        return toDeferred(this.getAcademicProgress(refresh))
    }

    public open suspend fun saveCourseByJs(data: CourseParamsJSONObject): Deferred<UTSJSONObject> {
        return toDeferred(
            this.saveCourse(
                CourseParams(
                    course = data.course,
                    hideRule = data.hideRule
                )
            )
        )
    }

    public open suspend fun deleteCourseByJs(data: DeleteCourseJSONObject): Deferred<UTSJSONObject> {
        return toDeferred(
            this.deleteCourse(
                DeleteCourse(
                    courseId = data.courseId,
                    isSystem = data.isSystem,
                    day = data.day,
                    start = data.start
                )
            )
        )
    }

    public open suspend fun restoreCourseByJs(data: RestoreCourseJSONObject): Deferred<UTSJSONObject> {
        return toDeferred(
            this.restoreCourse(
                RestoreCourse(
                    courseId = data.courseId,
                    day = data.day,
                    start = data.start
                )
            )
        )
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

    public open suspend fun initYiBanByJs(
        mobile: String,
        password: String
    ): Deferred<UTSJSONObject> {
        return toDeferred(this.initYiBan(mobile, password))
    }
}
