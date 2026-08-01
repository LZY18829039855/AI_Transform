package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.CoursePlanningInfoVO;
import com.huawei.aitransform.entity.DepartmentVO;
import com.huawei.aitransform.entity.DeptCourseSelection;
import com.huawei.aitransform.mapper.CoursePlanningInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI课程规划明细表服务类
 */
@Service
public class CoursePlanningInfoService {

    @Autowired
    private CoursePlanningInfoMapper coursePlanningInfoMapper;

    /**
     * 查询所有课程规划明细数据（含各部门选课回填 selectedDepts）
     */
    public List<CoursePlanningInfoVO> getAllCoursePlanningInfo() {
        List<CoursePlanningInfoVO> courses = coursePlanningInfoMapper.getAllCoursePlanningInfo();
        List<DeptCourseSelection> deptSelections = coursePlanningInfoMapper.getAllDeptSelections();
        Map<String, List<DepartmentVO>> courseIdToDeptsMap = buildCourseIdToDeptsMap(deptSelections);

        for (CoursePlanningInfoVO course : courses) {
            String courseIdStr = String.valueOf(course.getId());
            List<DepartmentVO> selectedDepts =
                    courseIdToDeptsMap.getOrDefault(courseIdStr, new ArrayList<>());
            course.setSelectedDepts(selectedDepts);
        }
        return courses;
    }

    /**
     * 管理端：查询全部课程主数据（不含部门选课聚合）
     */
    public List<CoursePlanningInfoVO> getAllCoursesForManage() {
        return coursePlanningInfoMapper.getAllCoursesForManage();
    }

    public CoursePlanningInfoVO getById(Integer id) {
        if (id == null) {
            return null;
        }
        return coursePlanningInfoMapper.getById(id);
    }

    public CoursePlanningInfoVO create(CoursePlanningInfoVO body) {
        if (body == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        normalizeCourse(body);
        validateCourse(body);
        body.setId(null);
        coursePlanningInfoMapper.insert(body);
        return coursePlanningInfoMapper.getById(body.getId());
    }

    public CoursePlanningInfoVO update(Integer id, CoursePlanningInfoVO body) {
        if (id == null) {
            throw new IllegalArgumentException("课程 ID 不能为空");
        }
        if (body == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        CoursePlanningInfoVO existing = coursePlanningInfoMapper.getById(id);
        if (existing == null) {
            return null;
        }
        normalizeCourse(body);
        validateCourse(body);
        body.setId(id);
        coursePlanningInfoMapper.updateById(body);
        return coursePlanningInfoMapper.getById(id);
    }

    public boolean delete(Integer id) {
        if (id == null) {
            return false;
        }
        CoursePlanningInfoVO existing = coursePlanningInfoMapper.getById(id);
        if (existing == null) {
            return false;
        }
        return coursePlanningInfoMapper.deleteById(id) > 0;
    }

    private void normalizeCourse(CoursePlanningInfoVO body) {
        body.setBigType(trimToNull(body.getBigType()));
        body.setSybType(trimToNull(body.getSybType()));
        body.setCourseName(trimToEmpty(body.getCourseName()));
        body.setCourseLink(trimToNull(body.getCourseLink()));
        body.setCredit(trimToNull(body.getCredit()));
        body.setCourseStatus(trimToNull(body.getCourseStatus()));
        body.setKnowledgePoint(trimToNull(body.getKnowledgePoint()));
        body.setCourseExplain(trimToNull(body.getCourseExplain()));
        body.setStudyDuration(trimToNull(body.getStudyDuration()));
        body.setCourseLevel(trimToNull(body.getCourseLevel()));
        body.setInClassTest(trimToNull(body.getInClassTest()));
        body.setCourseNumber(trimToNull(body.getCourseNumber()));
    }

    private void validateCourse(CoursePlanningInfoVO body) {
        if (!StringUtils.hasText(body.getCourseName())) {
            throw new IllegalArgumentException("课程名称不能为空");
        }
        if (!StringUtils.hasText(body.getCourseLevel())) {
            throw new IllegalArgumentException("训战分类不能为空");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 将部门选课（基础/进阶 + 实战）反转为 课程ID -> 部门列表
     */
    private Map<String, List<DepartmentVO>> buildCourseIdToDeptsMap(List<DeptCourseSelection> deptSelections) {
        Map<String, List<DepartmentVO>> courseIdToDeptsMap = new HashMap<>();
        Map<String, Set<String>> courseIdToDeptCodeSet = new HashMap<>();

        if (deptSelections == null) {
            return courseIdToDeptsMap;
        }

        for (DeptCourseSelection selection : deptSelections) {
            appendCourseIdsToDeptMap(
                    courseIdToDeptsMap,
                    courseIdToDeptCodeSet,
                    selection.getCourseSelections(),
                    selection);
            appendCourseIdsToDeptMap(
                    courseIdToDeptsMap,
                    courseIdToDeptCodeSet,
                    selection.getPracticalSelections(),
                    selection);
        }
        return courseIdToDeptsMap;
    }

    private void appendCourseIdsToDeptMap(
            Map<String, List<DepartmentVO>> courseIdToDeptsMap,
            Map<String, Set<String>> courseIdToDeptCodeSet,
            String courseSelectionsStr,
            DeptCourseSelection selection) {
        if (courseSelectionsStr == null || courseSelectionsStr.isEmpty()) {
            return;
        }
        String[] courseIds = courseSelectionsStr.split(",");
        for (String courseId : courseIds) {
            courseId = courseId.trim();
            if (courseId.isEmpty()) {
                continue;
            }
            Set<String> deptCodes = courseIdToDeptCodeSet.computeIfAbsent(courseId, k -> new HashSet<>());
            if (!deptCodes.add(selection.getDeptCode())) {
                continue;
            }
            List<DepartmentVO> depts = courseIdToDeptsMap.computeIfAbsent(courseId, k -> new ArrayList<>());
            depts.add(new DepartmentVO(selection.getDeptCode(), selection.getDeptName()));
        }
    }
}
