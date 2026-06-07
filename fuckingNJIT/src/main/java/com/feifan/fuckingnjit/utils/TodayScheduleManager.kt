package com.feifan.fuckingnjit.utils

import com.alibaba.fastjson.JSONArray
import com.feifan.fuckingnjit.model.Course
import com.feifan.fuckingnjit.model.User
import com.feifan.fuckingnjit.utils.database.AppDataCenter
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId


/**
 * 单节课的时间槽数据类（已转换为物理时间）
 *
 * @param id 课程ID
 * @param courseName 课程名称
 * @param classroom 上课地点
 * @param startTime 课程开始时间
 * @param endTime 课程结束时间
 * @param startNode 开始节次
 * @param step 持续节数
 */
data class DailyCourseSlot(
    val id: String,
    val courseName: String,
    val classroom: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val startNode: Int,
    val step: Int
)

/**
 * 今日课程表管理器
 *
 * 从用户缓存的课表数据中过滤出今天的课程并转换为物理时间槽，
 * 提供空堂查询、上课状态判断、当前课程定位等能力。
 * 内部按天缓存计算结果避免重复解析。
 */
object TodayScheduleManager {

    /** 缓存当天的物理时间槽位列表 */
    private var cachedSlots: List<DailyCourseSlot>? = null
    private var lastUpdateDay: Int = -1
    private var cachedCurrentWeek: Int = -1

    /**
     * 重新加载并解析当天的课程数据到时间槽缓存
     */
    private fun reloadTodaySlots() {
        // 这部分逻辑从小部件原封不动地搬过来
        val startDateStr = EduScheduleConfig.getSemesterStartDate()
        val startDateMilli =
            LocalDate.parse(startDateStr).atStartOfDay(ZoneId.systemDefault()).toInstant()
                .toEpochMilli()
        cachedCurrentWeek = EduScheduleConfig.calculateCurrentWeek(startDateMilli)
        AppDataCenter.updateSystemConfig { it.currentWeek = cachedCurrentWeek }// 顺手更新当前周
        val targetDay = Tools.todayWeekIndex() + 1

        val user = AppDataCenter.getCurrentUser() ?: User()

        val curriculumsStr = user.curriculums.getJSONObject("data")?.getString("validTimeCourses") ?: "[]"

        val allCurriculumData = JSONArray.parseArray(curriculumsStr, Course::class.java) ?: emptyList()

        // 过滤出今天的课
        val todayCourses = allCurriculumData.filter { course ->
            course.day == targetDay && course.weekList.contains(cachedCurrentWeek)
        }.sortedWith(Comparator { c1, c2 ->
            if (c1.startNode != c2.startNode) c1.startNode - c2.startNode else c1.name.compareTo(c2.name)
        })

        // 转换为带有真实 LocalTime 的 Slot
        val newSlots = mutableListOf<DailyCourseSlot>()
        for (course in todayCourses) {
            val startTime = EduScheduleConfig.getCourseStartTime(course.startNode)
            val endTime = EduScheduleConfig.getCourseEndTime(course.startNode, course.step)

            newSlots.add(
                DailyCourseSlot(
                    id = course.id,
                    courseName = course.name,
                    classroom = course.classroom,
                    startTime = startTime,
                    endTime = endTime,
                    startNode = course.startNode,
                    step = course.step
                )
            )
        }

        cachedSlots = newSlots
        lastUpdateDay = LocalDate.now().dayOfYear
    }

    /** 强制清空课程时间槽缓存（登录后调用） */
    fun clearCache() {
        cachedSlots = null
        lastUpdateDay = -1
    }

    /**
     * 获取今天尚未结束的课程列表（用于小部件展示）
     *
     * @return 未结束的课程时间槽列表，按开始时间排序
     */
    fun getRemainingCoursesForWidget(): List<DailyCourseSlot> {
        val todayDay = LocalDate.now().dayOfYear
        if (cachedSlots == null || lastUpdateDay != todayDay) {
            reloadTodaySlots()
        }

        val slots = cachedSlots ?: return emptyList()
        val nowTime = LocalTime.now()

        return slots.filter { it.endTime.isAfter(nowTime) }
    }

    /** 获取当前教学周次 */
    fun getCurrentWeek(): Int {
        val todayDay = LocalDate.now().dayOfYear
        if (cachedSlots == null || lastUpdateDay != todayDay) {
            reloadTodaySlots()
        }
        return cachedCurrentWeek
    }
}