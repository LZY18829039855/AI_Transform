package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.CourseCategoryStatisticsVO;
import com.huawei.aitransform.entity.CourseInfoByLevelVO;
import com.huawei.aitransform.entity.CourseInfoVO;
import com.huawei.aitransform.entity.EmployeeTrainingInfoPO;
import com.huawei.aitransform.entity.PersonalCourseCompletionResponseVO;
import com.huawei.aitransform.mapper.EmployeeTrainingInfoMapper;
import com.huawei.aitransform.mapper.PersonalCourseCompletionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 个人课程完成情况服务类
 */
@Service
public class PersonalCourseCompletionService {

    private static final String LEVEL_BASIC = "基础";
    private static final String LEVEL_ADVANCED = "进阶";
    private static final String LEVEL_HIGH = "高阶";
    private static final String LEVEL_PRACTICAL = "实战";

    @Autowired
    private PersonalCourseCompletionMapper personalCourseCompletionMapper;

    @Autowired
    private EmployeeTrainingInfoMapper employeeTrainingInfoMapper;

    /**
     * 查询个人课程完成情况
     *
     * @param empNum 员工工号（不带首字母）
     * @return 个人课程完成情况响应对象
     */
    public PersonalCourseCompletionResponseVO getPersonalCourseCompletion(String empNum) {
        // 1. 全量课程（按级别分组），用于 totalCourses 与 courseList
        List<CourseInfoByLevelVO> allCourses = personalCourseCompletionMapper.getCourseInfoByLevel();
        Map<String, List<CourseInfoByLevelVO>> allCoursesByLevel = allCourses.stream()
                .collect(Collectors.groupingBy(CourseInfoByLevelVO::getCourseLevel));

        // 2. 员工训战信息：目标课程数、完课课程 ID 列表
        EmployeeTrainingInfoPO training = employeeTrainingInfoMapper.selectByEmployeeNumber(empNum);

        Map<String, Integer> targetNumByLevel = new HashMap<>();
        Map<String, Set<Integer>> completedIdsByLevel = new HashMap<>();
        if (training != null) {
            targetNumByLevel.put(LEVEL_BASIC, nullToZero(training.getBasicTargetCoursesNum()));
            targetNumByLevel.put(LEVEL_ADVANCED, nullToZero(training.getAdvancedTargetCoursesNum()));
            targetNumByLevel.put(LEVEL_PRACTICAL, nullToZero(training.getPracticalTargetCoursesNum()));
            completedIdsByLevel.put(LEVEL_BASIC, parseCommaSeparatedCourseIds(training.getBasicCourses()));
            completedIdsByLevel.put(LEVEL_ADVANCED, parseCommaSeparatedCourseIds(training.getAdvancedCourses()));
            completedIdsByLevel.put(LEVEL_PRACTICAL, parseCommaSeparatedCourseIds(training.getPracticalCourses()));
        }
        targetNumByLevel.putIfAbsent(LEVEL_BASIC, 0);
        targetNumByLevel.putIfAbsent(LEVEL_ADVANCED, 0);
        targetNumByLevel.putIfAbsent(LEVEL_PRACTICAL, 0);
        completedIdsByLevel.putIfAbsent(LEVEL_BASIC, Collections.emptySet());
        completedIdsByLevel.putIfAbsent(LEVEL_ADVANCED, Collections.emptySet());
        completedIdsByLevel.putIfAbsent(LEVEL_PRACTICAL, Collections.emptySet());
        // 高阶在 t_employee_training_info 中无对应字段
        targetNumByLevel.put(LEVEL_HIGH, 0);
        completedIdsByLevel.put(LEVEL_HIGH, Collections.emptySet());

        // 3. 按固定顺序遍历级别，保证输出稳定
        List<CourseCategoryStatisticsVO> courseStatistics = new ArrayList<>();
        List<String> levelOrderList = new ArrayList<>();
        levelOrderList.add(LEVEL_BASIC);
        levelOrderList.add(LEVEL_ADVANCED);
        levelOrderList.add(LEVEL_HIGH);
        levelOrderList.add(LEVEL_PRACTICAL);
        // 规划中存在的其它级别排在后面
        for (String level : allCoursesByLevel.keySet()) {
            if (!levelOrderList.contains(level)) {
                levelOrderList.add(level);
            }
        }

        for (String courseLevel : levelOrderList) {
            List<CourseInfoByLevelVO> allCoursesInLevel = allCoursesByLevel.get(courseLevel);
            if (allCoursesInLevel == null || allCoursesInLevel.isEmpty()) {
                continue;
            }

            int targetCoursesCount = targetNumByLevel.getOrDefault(courseLevel, 0);
            Set<Integer> completedIdSet = completedIdsByLevel.getOrDefault(courseLevel, Collections.emptySet());

            // 实际完课数：训战表中该级别完课 ID 列表的去重数量（与规划表 id 对应）
            int completedFromTraining = completedIdSet.size();

            List<CourseInfoVO> courseList = new ArrayList<>();
            for (CourseInfoByLevelVO course : allCoursesInLevel) {
                Integer id = course.getId();
                boolean isCompleted = id != null && completedIdSet.contains(id);
                courseList.add(new CourseInfoVO(
                        course.getCourseName(),
                        course.getCourseNumber(),
                        isCompleted,
                        course.getBigType(),
                        true,
                        course.getCourseLink()));
            }

            // 展示用 completedCourses：以训战表 ID 列表为准；若与目录交集不一致仍以列表计数为主
            int completedCourses = completedFromTraining;

            double completionRate = 0.0;
            if (targetCoursesCount > 0) {
                completionRate = (double) completedCourses / targetCoursesCount * 100;
                BigDecimal bd = BigDecimal.valueOf(completionRate);
                bd = bd.setScale(2, RoundingMode.HALF_UP);
                completionRate = bd.doubleValue();
            }

            CourseCategoryStatisticsVO statistics = new CourseCategoryStatisticsVO();
            statistics.setCourseLevel(courseLevel);
            statistics.setTotalCourses(allCoursesInLevel.size());
            statistics.setTargetCourses(targetCoursesCount);
            statistics.setCompletedCourses(completedCourses);
            statistics.setCompletionRate(completionRate);
            statistics.setCourseList(courseList);

            courseStatistics.add(statistics);
        }

        // 未在规划表出现、但排序表要求的级别（通常不会发生）：若需要可补空块，当前跳过

        // 4. 员工姓名：优先训战表，否则员工同步表
        String empName = "";
        if (training != null && training.getLastName() != null && !training.getLastName().trim().isEmpty()) {
            empName = training.getLastName().trim();
        } else {
            String name = personalCourseCompletionMapper.getLastNameByEmployeeNumber(empNum);
            empName = name != null ? name : "";
        }

        PersonalCourseCompletionResponseVO response = new PersonalCourseCompletionResponseVO();
        response.setEmpNum(empNum);
        response.setEmpName(empName);
        response.setCourseStatistics(courseStatistics);

        return response;
    }

    private static int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 解析逗号分隔的课程主键 ID（ai_course_planning_info.id），去重、忽略非法项
     */
    private static Set<Integer> parseCommaSeparatedCourseIds(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptySet();
        }
        Set<Integer> ids = new HashSet<>();
        for (String part : raw.split(",")) {
            String s = part.trim();
            if (s.isEmpty()) {
                continue;
            }
            try {
                ids.add(Integer.parseInt(s));
            } catch (NumberFormatException ignored) {
                // 跳过非数字
            }
        }
        return ids.isEmpty() ? Collections.emptySet() : ids;
    }
}
