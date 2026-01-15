package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.CoursePlanningInfoVO;
import com.huawei.aitransform.entity.DepartmentVO;
import com.huawei.aitransform.entity.DeptCourseSelection;
import com.huawei.aitransform.mapper.CoursePlanningInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI课程规划明细表服务类
 */
@Service
public class CoursePlanningInfoService {

    @Autowired
    private CoursePlanningInfoMapper coursePlanningInfoMapper;

    /**
     * 查询所有课程规划明细数据
     * @return 课程规划明细列表
     */
    public List<CoursePlanningInfoVO> getAllCoursePlanningInfo() {
        // 1. 获取所有课程
        List<CoursePlanningInfoVO> courses = coursePlanningInfoMapper.getAllCoursePlanningInfo();
        
        // 2. 获取所有部门选课信息
        List<DeptCourseSelection> deptSelections = coursePlanningInfoMapper.getAllDeptSelections();
        
        // 3. 构建课程ID到部门列表的映射
        Map<String, List<DepartmentVO>> courseIdToDeptsMap = new HashMap<>();
        
        if (deptSelections != null) {
            for (DeptCourseSelection selection : deptSelections) {
                String courseSelectionsStr = selection.getCourseSelections();
                if (courseSelectionsStr != null && !courseSelectionsStr.isEmpty()) {
                    // 课程ID是用逗号分隔的
                    String[] courseIds = courseSelectionsStr.split(",");
                    for (String courseId : courseIds) {
                        courseId = courseId.trim();
                        if (!courseId.isEmpty()) {
                            List<DepartmentVO> depts = courseIdToDeptsMap.computeIfAbsent(courseId, k -> new ArrayList<>());
                            depts.add(new DepartmentVO(selection.getDeptCode(), selection.getDeptName()));
                        }
                    }
                }
            }
        }
        
        // 4. 填充 selectedDepts
        for (CoursePlanningInfoVO course : courses) {
            // CoursePlanningInfoVO 中的 id 是 Integer，转为 String 进行匹配
            String courseIdStr = String.valueOf(course.getId());
            List<DepartmentVO> selectedDepts = courseIdToDeptsMap.getOrDefault(courseIdStr, new ArrayList<>());
            course.setSelectedDepts(selectedDepts);
        }
        
        return courses;
    }
}
