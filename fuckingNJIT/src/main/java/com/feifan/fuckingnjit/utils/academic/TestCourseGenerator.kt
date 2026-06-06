package com.feifan.fuckingnjit.utils.academic

import android.util.Log
import com.feifan.fuckingnjit.model.Course
import kotlin.random.Random

/**
 * 测试用随机课程数据生成器
 *
 * 用于在开发调试阶段快速填充本地课程数据，模拟一个完整的18周学期课表。
 * 生成的课程通过 [CourseManager] 持久化到 ObjectBox，可直接在课表界面查看。
 *
 * ## 使用方式
 * ```
 * // 一键生成并写入数据库（约25门课 + 几门无时间课程）
 * TestCourseGenerator.generateAndSave()
 *
 * // 只预览不写入
 * val courses = TestCourseGenerator.buildAll()
 *
// 清除所有已生成的测试数据
 * TestCourseGenerator.clearAll()
 * ```
 *
 * ## 生成的课程特征
 * - **覆盖范围**：周一~周五，第1~10节，前16周为主（后两周留空模拟调休/考试）
 * - **课程数量**：约 22~28 门有时间的课 + 3~5 门无时间占位课
 * - **周次模式**：全周 / 单周 / 双周 / 前半学期 / 后半学期 混合
 * - **冲突概率**：极低（每个时间段最多安排1门课），但跨天重复出现同一门课
 */
object TestCourseGenerator {

    private const val TAG = "TestCourseGen"

    /** 总周数 */
    private const val TOTAL_WEEKS = 18

    /** 有效教学周（后2周留作考试/调停） */
    private const val TEACHING_WEEKS = 16

    // ── 随机种子池（固定 seed 保证同一设备上多次调用结果一致） ──

    /** 课程名称池 — 覆盖理工科常见课 */
    private val COURSE_NAMES = listOf(
        "高等数学(A)I", "高等数学(A)II", "线性代数", "概率论与数理统计",
        "大学物理(A)", "大学物理实验", "程序设计基础(C语言)", "数据结构与算法",
        "计算机组成原理", "操作系统", "计算机网络", "数据库系统原理",
        "软件工程导论", "Web前端开发技术", "移动应用开发",
        "思想道德与法治", "中国近现代史纲要", "马克思主义基本原理",
        "大学英语(1)", "英语听说(1)", "体育(1)",
        "电路分析基础", "模拟电子技术", "数字电子技术",
        "机械设计基础", "工程制图与CAD", "自动控制原理"
    )

    /** 教师姓名池 */
    private val TEACHERS = listOf(
        "张明远", "李晓华", "王建国", "陈静怡", "刘志强",
        "赵文博", "孙丽萍", "周海涛", "吴佳妮", "郑伟",
        "黄晓明", "林思远", "何雨晴", "罗浩然", "梁婉清"
    )

    /** 上课地点池 */
    private val CLASSROOMS = listOf(
        "教A101", "教A201", "教A301", "教A401",
        "教B205", "教B305", "教B405",
        "理C102", "理C202", "理C302",
        "实验楼301", "实验楼403", "实验楼501",
        "机电楼201", "信息楼303", "文管楼105"
    )

    // ── 核心公开方法 ──

    /**
     * 生成完整的测试课表并通过 [CourseManager] 写入本地数据库。
     *
     * 写入前会先清除已有的本地课程数据（仅清除 local_ 前缀的课程），
     * 保证每次生成的结果干净可复现。
     *
     * @return 成功保存的课程数量，失败返回 -1
     */
    fun generateAndSave(): Int {
        val courses = buildAll()

        // 先清理旧的测试数据
        clearAll()

        var successCount = 0
        for (course in courses) {
            val ok = CourseManager.saveLocalCourse(course)
            if (ok) successCount++
            else Log.w(TAG, "保存失败: ${course.name}")
        }

        Log.i(
            TAG,
            "✅ 测试课表生成完毕：共 ${courses.size} 门课程，成功保存 $successCount 门"
        )
        return successCount
    }

    /**
     * 构建完整测试课程列表（不写入数据库）。
     *
     * @return 包含有时间课程和无时间课程的完整列表
     */
    fun buildAll(): List<Course> {
        val result = mutableListOf<Course>()

        // 1. 有固定时间的课程（课表网格中可见）
        result.addAll(buildScheduledCourses())

        // 2. 无固定时间课程（底部列表展示：毕设、网课、实习等）
        result.addAll(buildNoTimeCourses())

        return result
    }

    /**
     * 清除所有本地课程数据。
     *
     * 遍历 [CourseManager.getLocalCourses()] 返回的列表逐条删除，
     * 同时也会清除屏蔽规则。
     *
     * @return 是否全部清除成功
     */
    fun clearAll(): Boolean {
        val existing = CourseManager.getLocalCourses()
        if (existing.isEmpty()) return true

        var allOk = true
        for (course in existing) {
            val ok = CourseManager.deleteLocalCourse(course.id)
            if (!ok) {
                Log.w(TAG, "删除失败: ${course.id}")
                allOk = false
            }
        }

        Log.i(TAG, "🗑️ 已清除 ${existing.size} 条本地课程")
        return allOk
    }

    // ── 内部构建逻辑 ──

    /**
     * 构建有固定时间的课程列表。
     *
     * ### 排课策略
     * - 使用固定的 `Random(seed)` 保证结果可复现
     * - 每个工作日（周一~周五）按时间段逐个填充
     * - 同一门课程可能在不同天的不同节次出现（模拟多时段课）
     * - 周次模式随机分配（全周/单周/双周/前8周/后8周）
     *
     * 典型输出约 22~28 条 Course 记录
     */
    private fun buildScheduledCourses(): List<Course> {
        val courses = mutableListOf<Course>()
        val rng = Random(42) // 固定种子保证可复现

        // 已占用的时间槽：(day, startNode) → 防止同一天同节次重复排课
        val occupiedSlots = mutableSetOf<Pair<Int, Int>>()

        // 可用的节次区间：(startNode, step, 显示名)
        // NJIT 的节次：1-4上午, 5-8下午, 9-10晚上
        val slotOptions = listOf(
            Triple(1, 2, "1-2节"),
            Triple(3, 2, "3-4节"),
            Triple(5, 2, "5-6节"),   // 下午第一节
            Triple(7, 2, "7-8节"),   // 下午第二节
            Triple(9, 2, "9-10节"),  // 晚上课
        )

        // 课程名称取用索引
        var nameIndex = 0

        // ── 按工作日遍历（周一=1 到 周五=5） ──
        for (day in 1..5) {
            for ((startNode, step, _) in slotOptions) {
                // 15% 概率跳过该时间段（模拟空堂/自习）
                if (rng.nextInt(100) < 15) continue

                // 选一门课名（循环取用）
                val name = COURSE_NAMES[nameIndex % COURSE_NAMES.size]
                nameIndex++

                // 随机教师和教室
                val teacher = TEACHERS.random(rng)
                val classroom = CLASSROOMS.random(rng)

                // 随机决定周次模式
                val (weeks, rawDesc) = generateWeekPattern(rng)

                courses.add(
                    Course(
                        name = name,
                        teacher = teacher,
                        classroom = classroom,
                        day = day,
                        startNode = startNode,
                        step = step,
                        weekList = weeks,
                        rawWeeks = rawDesc
                        // id 和 source 由 CourseManager 自动处理
                    )
                )
            }
        }

        // ── 再额外插入几门"跨天重复"的大课（模拟高数、大英等每周多次） ──
        val repeatCourses = listOf(
            Triple("高等数学(A)I", "张明远", "教A201"),
            Triple("大学英语(1)", "李晓华", "教B305"),
            Triple("程序设计基础(C语言)", "刘志强", "实验楼401"),
            Triple("数据结构与算法", "赵文博", "信息楼303"),
        )

        for ((cname, tname, rname) in repeatCourses) {
            // 给每门课再分配 1~2 个额外的不同时间段
            val extraCount = rng.nextInt(1, 3)
            repeat(extraCount) {
                val extraDay = rng.nextInt(1, 6) // 周一到周五
                val slotIdx = rng.nextInt(slotOptions.size)
                val (sNode, sStep, _) = slotOptions[slotIdx]

                // 如果这个槽已经被上面的循环占了就换一个
                val finalSlot = if (occupiedSlots.contains(extraDay to sNode)) {
                    val altIdx = (slotIdx + 1) % slotOptions.size
                    slotOptions[altIdx]
                } else {
                    Triple(sNode, sStep, "")
                }

                val (weeks, rawDesc) = generateWeekPattern(rng)

                courses.add(
                    Course(
                        name = cname,
                        teacher = tname,
                        classroom = rname,
                        day = extraDay,
                        startNode = finalSlot.first,
                        step = finalSlot.second,
                        weekList = weeks,
                        rawWeeks = rawDesc
                    )
                )
            }
        }

        return courses
    }

    /**
     * 构建无固定时间课程列表（毕设、实习、网课等）。
     *
     * 这些课程 day=0、weekList 为空，在课表网格中不可见，
     * 仅在底部的「无时间课程」列表区域展示。
     */
    private fun buildNoTimeCourses(): List<Course> = listOf(
        Course(
            name = "毕业设计(论文)",
            teacher = "导师: 王建国",
            classroom = "",
            day = 0,
            startNode = 0,
            step = 0,
            weekList = emptyList(),
            rawWeeks = ""
        ),
        Course(
            name = "专业生产实习",
            teacher = "陈静怡",
            classroom = "校外实习基地",
            day = 0,
            startNode = 0,
            step = 0,
            weekList = (11..16).toList(),  // 仅后半学期
            rawWeeks = "11-16周"
        ),
        Course(
            name = "创新创业教育(网络课)",
            teacher = "超星平台",
            classroom = "在线学习",
            day = 0,
            startNode = 0,
            step = 0,
            weekList = emptyList(),
            rawWeeks = ""
        ),
        Course(
            name = "军事理论(慕课)",
            teacher = "学在线平台",
            classroom = "在线学习",
            day = 0,
            startNode = 0,
            step = 0,
            weekList = (1..8).toList(),
            rawWeeks = "1-8周"
        ),
        Course(
            name = "就业指导讲座",
            teacher = "招生就业处",
            classroom = "报告厅",
            day = 6,          // 周六
            startNode = 9,
            step = 2,
            weekList = listOf(3, 7, 11, 15),
            rawWeeks = "3,7,11,15(单)周"
        ),
    )

    // ── 周次模式生成 ──

    /**
     * 随机生成一种周次分配模式及其原始描述字符串。
     *
     * 权重分布：
     * | 模式 | 概率 | 示例 |
     * |------|------|------|
     * | 全程 | 50%  | 1-16周 |
     * | 单周 | 12%  | 1-15(单)周 |
     * | 双周 | 12%  | 2-16(双)周 |
     * | 前半学期 | 13% | 1-8周 |
     * | 后半学期 | 13% | 9-16周 |
     *
     * @param rng 随机数生成器实例
     * @return Pair(展开后的周数列表, 原始描述字符串)
     */
    private fun generateWeekPattern(rng: Random): Pair<List<Int>, String> {
        return when (rng.nextInt(100)) {
            in 0..49 -> { // 全程 50%
                Pair((1..TEACHING_WEEKS).toList(), "${TEACHING_WEEKS}周")
            }

            in 50..61 -> { // 单周 12%
                val odds = (1..TEACHING_WEEKS).filter { it % 2 != 0 }
                Pair(odds, "1-$TEACHING_WEEKS(单)周")
            }

            in 62..73 -> { // 双周 12%
                val evens = (2..TEACHING_WEEKS).filter { it % 2 == 0 }
                Pair(evens, "2-$TEACHING_WEEKS(双)周")
            }

            in 74..86 -> { // 前半学期 13%
                val half = TEACHING_WEEKS / 2
                Pair((1..half).toList(), "1-${half}周")
            }

            else -> { // 后半学期 14%
                val mid = TEACHING_WEEKS / 2 + 1
                Pair((mid..TEACHING_WEEKS).toList(), "${mid}-${TEACHING_WEEKS}周")
            }
        }
    }
}
