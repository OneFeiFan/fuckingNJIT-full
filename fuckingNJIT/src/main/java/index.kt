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
import com.feifan.fuckingnjit.utils.network.wifiauth.PortalManager
import com.feifan.fuckingnjit.utils.system.SystemActionHelper
import com.feifan.fuckingnjit.widget.CurriculumsWidget
import io.dcloud.uts.JSON
import io.dcloud.uts.JsonNotNull
import io.dcloud.uts.UTSAndroid
import io.dcloud.uts.UTSError
import io.dcloud.uts.UTSJSONObject
import io.dcloud.uts.UTSObject
import io.dcloud.uts.UTSPromise
import io.dcloud.uts.await
import io.dcloud.uts.console
import io.dcloud.uts.toDeferred
import io.dcloud.uts.wrapUTSPromise
import kotlinx.coroutines.Deferred

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
    open fun showToast(str: String) {
        SystemActionHelper.showToast(UTSAndroid.getAppContext()!!, str)
    }

    open fun startLogin(relogin: Boolean) {
        SystemActionHelper.startLogin(UTSAndroid.getAppContext()!!, relogin)
    }

    open fun getCurriculum(refresh: Boolean): UTSPromise<UTSJSONObject> {
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

    open fun getAllUsers(): String {
        return Manager.getUserManager().getAllUsers(UTSAndroid.getAppContext()!!)
    }

    open fun setCurrentUser(id: String) {
        return Manager.getUserManager().setCurrentUser(UTSAndroid.getAppContext()!!, id)
    }

    open fun deleteUser(id: String): UTSPromise<Boolean> {
        return wrapUTSPromise(suspend w@{
            return@w await(Manager.getUserManager().deleteUser(UTSAndroid.getAppContext()!!, id))
        })
    }

    open fun getEmptyClassrooms(
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

    open fun getSorces(
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

    open fun getSorcesDetail(
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

    open fun getAllExam(): UTSPromise<UTSJSONObject> {
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

    open fun getAcademicProgress(refresh: Boolean): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
            val data = await(
                Manager.getUserManager().getAcademicProgress(UTSAndroid.getAppContext()!!, refresh)
                    .toJSONString()
            )
            return@w parseUTSResponse(data)
        })
    }

    open fun saveCourse(data: CourseParams): UTSPromise<UTSJSONObject> {
        return wrapUTSPromise(suspend w@{
            val result = await(
                Manager.getWebService()
                    .saveCourse(UTSAndroid.getAppContext()!!, data.course, data.hideRule)
                    .toJSONString()
            )
            return@w parseUTSResponse(result)
        })
    }

    open fun deleteCourse(data: DeleteCourse): UTSPromise<UTSJSONObject> {
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

    open fun restoreCourse(data: RestoreCourse): UTSPromise<UTSJSONObject> {
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

    open fun getDate(): String {
        return Manager.getWebService().getDate(UTSAndroid.getAppContext()!!)
    }

    open fun createWidget(): String {
        return CurriculumsWidget(UTSAndroid.getAppContext()!!).createWidget()
    }

    open fun getWidgetPermission() {
        return CurriculumsWidget(UTSAndroid.getAppContext()!!).getPermission()
    }

    open fun isWidgetAlreadyCreated(): Boolean {
        return CurriculumsWidget(UTSAndroid.getAppContext()!!).isWidgetAlreadyCreated()
    }

    open fun switchStatus(status: Boolean) {
        PortalManager.switchStatus(UTSAndroid.getAppContext()!!, status)
    }

    open fun isEnabled(): Boolean {
        return PortalManager.isEnabled(UTSAndroid.getAppContext()!!)
    }

    open fun setWifiAuthType(type: String) {
        AppConfig.setWifiAuthType(type)
    }

    open fun getWifiAuthType(): String {
        return AppConfig.getWifiAuthType()
    }

    open fun goHome() {
        SystemActionHelper.goHome(UTSAndroid.getAppContext()!!)
    }

    open fun setPasswordStorageEnabled(enable: Boolean) {
        Manager.getUserManager().setPasswordStorageEnabled(enable)
    }

    open fun isPasswordStorageEnabled(): Boolean {
        return Manager.getUserManager().isPasswordStorageEnabled()
    }

    open fun updateApp(url: String): UTSPromise<Boolean> {
        return wrapUTSPromise(suspend w@{
            return@w await(SystemActionHelper.updateApp(UTSAndroid.getAppContext()!!, url))
        })
    }

    open fun checkInstallPackagePermission(): Boolean {
        return Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!)
            .checkRequestInstallPackage()
    }

    open fun requestInstallPackage(): UTSPromise<Boolean> {
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

    open fun isSmartUpdate(): Boolean {
        return Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).isSmartUpdate()
    }

    open fun setSmartUpdate(isSmart: Boolean) {
        Manager.getPermissionsManager(UTSAndroid.getUniActivity()!!).setSmartUpdate(isSmart)
    }

    open fun initYiBan(mobile: String, password: String): UTSPromise<UTSJSONObject> {
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
    constructor() : super()

    open fun showToastByJs(str: String) {
        return this.showToast(str)
    }

    open fun startLoginByJs(relogin: Boolean) {
        return this.startLogin(relogin)
    }

    open suspend fun getCurriculumByJs(refresh: Boolean): Deferred<UTSJSONObject> {
        return toDeferred(this.getCurriculum(refresh))
    }

    open fun getAllUsersByJs(): String {
        return this.getAllUsers()
    }

    open fun setCurrentUserByJs(id: String) {
        return this.setCurrentUser(id)
    }

    open suspend fun deleteUserByJs(id: String): Deferred<Boolean> {
        return toDeferred(this.deleteUser(id))
    }

    open suspend fun getEmptyClassroomsByJs(
        dateRange: String,
        coursePeriod: String,
        buildingId: String
    ): Deferred<UTSJSONObject> {
        return toDeferred(this.getEmptyClassrooms(dateRange, coursePeriod, buildingId))
    }

    open suspend fun getSorcesByJs(
        xnm: String,
        xqm: String,
        refresh: Boolean
    ): Deferred<UTSJSONObject> {
        return toDeferred(this.getSorces(xnm, xqm, refresh))
    }

    open suspend fun getSorcesDetailByJs(
        classId: String,
        schoolYear: String,
        semester: String,
        courseName: String
    ): Deferred<UTSJSONObject> {
        return toDeferred(this.getSorcesDetail(classId, schoolYear, semester, courseName))
    }

    open suspend fun getAllExamByJs(): Deferred<UTSJSONObject> {
        return toDeferred(this.getAllExam())
    }

    open suspend fun getAcademicProgressByJs(refresh: Boolean): Deferred<UTSJSONObject> {
        return toDeferred(this.getAcademicProgress(refresh))
    }

    open suspend fun saveCourseByJs(data: CourseParamsJSONObject): Deferred<UTSJSONObject> {
        return toDeferred(
            this.saveCourse(
                CourseParams(
                    course = data.course,
                    hideRule = data.hideRule
                )
            )
        )
    }

    open suspend fun deleteCourseByJs(data: DeleteCourseJSONObject): Deferred<UTSJSONObject> {
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

    open suspend fun restoreCourseByJs(data: RestoreCourseJSONObject): Deferred<UTSJSONObject> {
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

    open fun getDateByJs(): String {
        return this.getDate()
    }

    open fun createWidgetByJs(): String {
        return this.createWidget()
    }

    open fun getWidgetPermissionByJs() {
        return this.getWidgetPermission()
    }

    open fun isWidgetAlreadyCreatedByJs(): Boolean {
        return this.isWidgetAlreadyCreated()
    }

    open fun switchStatusByJs(status: Boolean) {
        return this.switchStatus(status)
    }

    open fun isEnabledByJs(): Boolean {
        return this.isEnabled()
    }

    open fun setWifiAuthTypeByJs(type: String) {
        return this.setWifiAuthType(type)
    }

    open fun getWifiAuthTypeByJs(): String {
        return this.getWifiAuthType()
    }

    open fun goHomeByJs() {
        return this.goHome()
    }

    open fun setPasswordStorageEnabledByJs(enable: Boolean) {
        return this.setPasswordStorageEnabled(enable)
    }

    open fun isPasswordStorageEnabledByJs(): Boolean {
        return this.isPasswordStorageEnabled()
    }

    open suspend fun updateAppByJs(url: String): Deferred<Boolean> {
        return toDeferred(this.updateApp(url))
    }

    open fun checkInstallPackagePermissionByJs(): Boolean {
        return this.checkInstallPackagePermission()
    }

    open suspend fun requestInstallPackageByJs(): Deferred<Boolean> {
        return toDeferred(this.requestInstallPackage())
    }

    open fun isSmartUpdateByJs(): Boolean {
        return this.isSmartUpdate()
    }

    open fun setSmartUpdateByJs(isSmart: Boolean) {
        return this.setSmartUpdate(isSmart)
    }

    open suspend fun initYiBanByJs(
        mobile: String,
        password: String
    ): Deferred<UTSJSONObject> {
        return toDeferred(this.initYiBan(mobile, password))
    }
}
