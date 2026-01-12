package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.CourseCategoryStatisticsVO;
import com.huawei.aitransform.entity.CourseInfoByLevelVO;
import com.huawei.aitransform.entity.CourseInfoVO;
import com.huawei.aitransform.entity.PersonalCourseCompletionResponseVO;
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

    /**
     * 查询个人课程完成情况
     * @param empNum 员工工号（不带首字母）
     * @return 个人课程完成情况响应对象
     */
    public PersonalCourseCompletionResponseVO getPersonalCourseCompletion(String empNum) {
        // 查询所有课程信息（按级别分类）
        List<CourseInfoByLevelVO> allCourses = personalCourseCompletionMapper.getCourseInfoByLevel();

        // 按课程级别分组
        Map<String, List<CourseInfoByLevelVO>> coursesByLevel = allCourses.stream()
                .collect(Collectors.groupingBy(CourseInfoByLevelVO::getCourseLevel));

        // 获取所有课程编码
        List<String> allCourseNumbers = allCourses.stream()
                .map(CourseInfoByLevelVO::getCourseNumber)
                .distinct()
                .collect(Collectors.toList());

        // 查询用户已完成的课程编码列表
        List<String> completedCourseNumbers = new ArrayList<>();
        if (!allCourseNumbers.isEmpty()) {
            completedCourseNumbers = personalCourseCompletionMapper.getCompletedCourseNumbers(empNum, allCourseNumbers);
        }

        // 将已完成的课程编码转换为Set，便于快速查找
        Map<String, Boolean> completedCourseMap = new HashMap<>();
        for (String courseNumber : completedCourseNumbers) {
            completedCourseMap.put(courseNumber, true);
        }

        // 组装各分类的课程统计信息
        List<CourseCategoryStatisticsVO> courseStatistics = new ArrayList<>();
        for (Map.Entry<String, List<CourseInfoByLevelVO>> entry : coursesByLevel.entrySet()) {
            String courseLevel = entry.getKey();
            List<CourseInfoByLevelVO> courses = entry.getValue();

            // 构建课程列表
            List<CourseInfoVO> courseList = new ArrayList<>();
            int completedCount = 0;
            for (CourseInfoByLevelVO course : courses) {
                Boolean isCompleted = completedCourseMap.containsKey(course.getCourseNumber());
                if (isCompleted) {
                    completedCount++;
                }
                // 为每门课程设置 bigType、isTargetCourse 和 courseLink
                courseList.add(new CourseInfoVO(course.getCourseName(), course.getCourseNumber(), isCompleted, 
                        course.getBigType(), course.getIsTargetCourse(), course.getCourseLink()));
            }

            // 计算完课占比
            int totalCourses = courses.size();
            double completionRate = 0.0;
            if (totalCourses > 0) {
                completionRate = (double) completedCount / totalCourses * 100;
                // 保留2位小数
                BigDecimal bd = new BigDecimal(completionRate);
                bd = bd.setScale(2, RoundingMode.HALF_UP);
                completionRate = bd.doubleValue();
            }

            // 创建分类统计对象
            CourseCategoryStatisticsVO statistics = new CourseCategoryStatisticsVO();
            statistics.setCourseLevel(courseLevel);
            statistics.setTotalCourses(totalCourses);
            statistics.setTargetCourses(totalCourses);
            statistics.setCompletedCourses(completedCount);
            statistics.setCompletionRate(completionRate);
            statistics.setCourseList(courseList);

            courseStatistics.add(statistics);
        }

        // 按照指定顺序排序：基础、进阶、高阶、实战
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

        // 查询员工中文名
        String empName = personalCourseCompletionMapper.getLastNameByEmployeeNumber(empNum);
        if (empName == null) {
            empName = "";
        }

        // 创建响应对象
        PersonalCourseCompletionResponseVO response = new PersonalCourseCompletionResponseVO();
        response.setEmpNum(empNum);
        response.setEmpName(empName);
        response.setCourseStatistics(courseStatistics);

        return response;
    }
}

