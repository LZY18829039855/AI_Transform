package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.CourseCategoryStatisticsVO;
import com.huawei.aitransform.entity.CourseInfoByLevelVO;
import com.huawei.aitransform.entity.CourseInfoVO;
import com.huawei.aitransform.entity.DeptCourseSelection;
import com.huawei.aitransform.entity.PersonalCourseCompletionResponseVO;
import com.huawei.aitransform.mapper.CoursePlanningInfoMapper;
import com.huawei.aitransform.mapper.PersonalCourseCompletionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 个人课程完成情况服务类
 */
@Service
public class PersonalCourseCompletionService {

    @Autowired
    private PersonalCourseCompletionMapper personalCourseCompletionMapper;

    @Autowired
    private CoursePlanningInfoMapper coursePlanningInfoMapper;

    /**
     * 查询个人课程完成情况
     * @param empNum 员工工号（不带首字母）
     * @return 个人课程完成情况响应对象
     */
    public PersonalCourseCompletionResponseVO getPersonalCourseCompletion(String empNum) {
        // 1. 查询所有课程信息（用于计算totalCourses）
        List<CourseInfoByLevelVO> allCourses = personalCourseCompletionMapper.getCourseInfoByLevel();
        Map<String, List<CourseInfoByLevelVO>> allCoursesByLevel = allCourses.stream()
                .collect(Collectors.groupingBy(CourseInfoByLevelVO::getCourseLevel));
        
        // 2. 查询员工四级部门ID
        String fourthDeptCode = personalCourseCompletionMapper.getFourthDeptCodeByEmployeeNumber(empNum);
        
        // 3. 获取部门选定的目标课程
        List<CourseInfoByLevelVO> targetCourses;
        boolean useAllCourses = false; // 标记是否使用所有课程
        
        if (fourthDeptCode != null && !fourthDeptCode.trim().isEmpty()) {
            // 根据部门ID直接查询部门选课信息
            DeptCourseSelection userDeptSelection = coursePlanningInfoMapper.getDeptSelectionByDeptCode(fourthDeptCode);
            
            // 解析目标课程ID列表
            List<Integer> targetCourseIds = new ArrayList<>();
            if (userDeptSelection != null && userDeptSelection.getCourseSelections() != null 
                    && !userDeptSelection.getCourseSelections().trim().isEmpty()) {
                // 部门有选课信息，解析选定的课程ID
                String courseSelectionsStr = userDeptSelection.getCourseSelections();
                String[] courseIdStrs = courseSelectionsStr.split(",");
                for (String courseIdStr : courseIdStrs) {
                    courseIdStr = courseIdStr.trim();
                    if (!courseIdStr.isEmpty()) {
                        try {
                            targetCourseIds.add(Integer.parseInt(courseIdStr));
                        } catch (NumberFormatException e) {
                            // 忽略无效的课程ID
                        }
                    }
                }
                // 如果解析后没有有效的课程ID，则使用所有课程
                if (targetCourseIds.isEmpty()) {
                    useAllCourses = true;
                }
            } else {
                // 部门没有选课信息，使用所有课程作为默认目标课程
                useAllCourses = true;
            }
            
            // 根据标志位决定查询方式
            if (useAllCourses) {
                // 使用所有课程作为目标课程（fallback逻辑）
                targetCourses = personalCourseCompletionMapper.getCourseInfoByLevel();
            } else {
                // 根据目标课程ID列表查询课程信息
                targetCourses = personalCourseCompletionMapper.getCourseInfoByLevelAndIds(targetCourseIds);
            }
        } else {
            // 如果未找到部门信息，使用所有课程作为默认目标课程
            targetCourses = personalCourseCompletionMapper.getCourseInfoByLevel();
        }

        // 4. 按课程级别分组目标课程
        Map<String, List<CourseInfoByLevelVO>> targetCoursesByLevel = targetCourses.stream()
                .collect(Collectors.groupingBy(CourseInfoByLevelVO::getCourseLevel));

        // 4. 获取目标课程编码列表
        List<String> targetCourseNumbers = targetCourses.stream()
                .map(CourseInfoByLevelVO::getCourseNumber)
                .distinct()
                .collect(Collectors.toList());

        // 5. 查询用户已完成的课程编码列表（只查询目标课程中的完课数据）
        List<String> completedCourseNumbers = new ArrayList<>();
        if (!targetCourseNumbers.isEmpty()) {
            completedCourseNumbers = personalCourseCompletionMapper.getCompletedCourseNumbers(empNum, targetCourseNumbers);
        }

        // 6. 将已完成的课程编码转换为Map，便于快速查找
        Map<String, Boolean> completedCourseMap = new HashMap<>();
        for (String courseNumber : completedCourseNumbers) {
            completedCourseMap.put(courseNumber, true);
        }

        // 7. 组装各分类的课程统计信息
        List<CourseCategoryStatisticsVO> courseStatistics = new ArrayList<>();
        
        // 遍历所有课程级别，确保每个级别都有统计信息
        for (Map.Entry<String, List<CourseInfoByLevelVO>> entry : allCoursesByLevel.entrySet()) {
            String courseLevel = entry.getKey();
            List<CourseInfoByLevelVO> allCoursesInLevel = entry.getValue();
            
            // 获取该级别下的目标课程
            List<CourseInfoByLevelVO> targetCoursesInLevel = targetCoursesByLevel.getOrDefault(courseLevel, new ArrayList<>());
            
            // 构建目标课程列表
            List<CourseInfoVO> courseList = new ArrayList<>();
            int completedCount = 0;
            for (CourseInfoByLevelVO course : targetCoursesInLevel) {
                Boolean isCompleted = completedCourseMap.containsKey(course.getCourseNumber());
                if (isCompleted) {
                    completedCount++;
                }
                // 为每门课程设置 bigType、isTargetCourse（固定为true）和 courseLink
                courseList.add(new CourseInfoVO(course.getCourseName(), course.getCourseNumber(), isCompleted, 
                        course.getBigType(), true, course.getCourseLink()));
            }

            // 计算完课占比（基于目标课程数）
            int totalCourses = allCoursesInLevel.size(); // 所有课程数量
            int targetCoursesCount = targetCoursesInLevel.size(); // 目标课程数量
            double completionRate = 0.0;
            if (targetCoursesCount > 0) {
                completionRate = (double) completedCount / targetCoursesCount * 100;
                // 保留2位小数
                BigDecimal bd = new BigDecimal(completionRate);
                bd = bd.setScale(2, RoundingMode.HALF_UP);
                completionRate = bd.doubleValue();
            }

            // 创建分类统计对象
            CourseCategoryStatisticsVO statistics = new CourseCategoryStatisticsVO();
            statistics.setCourseLevel(courseLevel);
            statistics.setTotalCourses(totalCourses); // 所有课程数量
            statistics.setTargetCourses(targetCoursesCount); // 目标课程数量
            statistics.setCompletedCourses(completedCount);
            statistics.setCompletionRate(completionRate);
            statistics.setCourseList(courseList);

            courseStatistics.add(statistics);
        }

        // 8. 按照指定顺序排序：基础、进阶、高阶、实战
        Map<String, Integer> levelOrder = new HashMap<>();
        levelOrder.put("基础", 1);
        levelOrder.put("进阶", 2);
        levelOrder.put("高阶", 3);
        levelOrder.put("实战", 4);
        
        // 对courseStatistics进行排序
        courseStatistics.sort((a, b) -> {
            String levelA = a.getCourseLevel();
            String levelB = b.getCourseLevel();
            Integer orderA = levelOrder.getOrDefault(levelA, 999); // 未匹配的排在最后
            Integer orderB = levelOrder.getOrDefault(levelB, 999);
            return orderA.compareTo(orderB);
        });

        // 9. 查询员工中文名
        String empName = personalCourseCompletionMapper.getLastNameByEmployeeNumber(empNum);
        if (empName == null) {
            empName = "";
        }

        // 10. 创建响应对象
        PersonalCourseCompletionResponseVO response = new PersonalCourseCompletionResponseVO();
        response.setEmpNum(empNum);
        response.setEmpName(empName);
        response.setCourseStatistics(courseStatistics);

        return response;
    }
}

