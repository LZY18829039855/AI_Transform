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
        if (!StringUtils.hasText(body.getBigType())) {
            throw new IllegalArgumentException("课程主分类不能为空");
        }
        if (!StringUtils.hasText(body.getCourseNumber())) {
            throw new IllegalArgumentException("课程编码不能为空");
        }
        if (!StringUtils.hasText(body.getCourseLink())) {
            throw new IllegalArgumentException("课程链接不能为空");
        }
        if (!StringUtils.hasText(body.getCredit())) {
            throw new IllegalArgumentException("学分不能为空");
        }
        String credit = body.getCredit().trim();
        if (!credit.matches("^\\d+(\\.\\d+)?$")) {
            throw new IllegalArgumentException("学分仅支持整数或小数");
        }
        body.setCredit(credit);
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

    /** ---------- 部门目标选课 CRUD ---------- */

    public List<DeptCourseSelection> listDeptSelections() {
        List<DeptCourseSelection> list = coursePlanningInfoMapper.getAllDeptSelections();
        return list == null ? new ArrayList<>() : list;
    }

    public DeptCourseSelection getDeptSelection(String deptCode) {
        if (!StringUtils.hasText(deptCode)) {
            return null;
        }
        return coursePlanningInfoMapper.getDeptSelectionByDeptCode(deptCode.trim());
    }

    public DeptCourseSelection createDeptSelection(DeptCourseSelection body) {
        DeptCourseSelection normalized = normalizeDeptSelection(body);
        validateDeptSelection(normalized, true);
        DeptCourseSelection existing =
                coursePlanningInfoMapper.getDeptSelectionByDeptCode(normalized.getDeptCode());
        if (existing != null) {
            throw new IllegalArgumentException("该部门选课配置已存在，请使用更新接口");
        }
        fillTargetNumsIfAbsent(normalized);
        coursePlanningInfoMapper.insertDeptSelection(normalized);
        return coursePlanningInfoMapper.getDeptSelectionByDeptCode(normalized.getDeptCode());
    }

    public DeptCourseSelection updateDeptSelection(String deptCode, DeptCourseSelection body) {
        if (!StringUtils.hasText(deptCode)) {
            throw new IllegalArgumentException("部门编码不能为空");
        }
        String code = deptCode.trim();
        DeptCourseSelection existing = coursePlanningInfoMapper.getDeptSelectionByDeptCode(code);
        if (existing == null) {
            return null;
        }
        DeptCourseSelection normalized = normalizeDeptSelection(body);
        normalized.setDeptCode(code);
        if (!StringUtils.hasText(normalized.getDeptName())) {
            normalized.setDeptName(existing.getDeptName());
        }
        validateDeptSelection(normalized, false);
        fillTargetNumsIfAbsent(normalized);
        coursePlanningInfoMapper.updateDeptSelectionByDeptCode(normalized);
        return coursePlanningInfoMapper.getDeptSelectionByDeptCode(code);
    }

    public boolean deleteDeptSelection(String deptCode) {
        if (!StringUtils.hasText(deptCode)) {
            return false;
        }
        String code = deptCode.trim();
        DeptCourseSelection existing = coursePlanningInfoMapper.getDeptSelectionByDeptCode(code);
        if (existing == null) {
            return false;
        }
        return coursePlanningInfoMapper.deleteDeptSelectionByDeptCode(code) > 0;
    }

    private DeptCourseSelection normalizeDeptSelection(DeptCourseSelection body) {
        if (body == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        DeptCourseSelection normalized = new DeptCourseSelection();
        normalized.setDeptCode(trimToEmpty(body.getDeptCode()));
        normalized.setDeptName(trimToNull(body.getDeptName()));
        normalized.setCourseSelections(normalizeIdList(body.getCourseSelections()));
        normalized.setPracticalSelections(normalizeIdList(body.getPracticalSelections()));
        normalized.setBasicTargetCoursesNum(body.getBasicTargetCoursesNum());
        normalized.setAdvancedTargetCoursesNum(body.getAdvancedTargetCoursesNum());
        normalized.setPracticalTargetCoursesNum(body.getPracticalTargetCoursesNum());
        return normalized;
    }

    private void validateDeptSelection(DeptCourseSelection body, boolean requireName) {
        if (!StringUtils.hasText(body.getDeptCode())) {
            throw new IllegalArgumentException("部门编码不能为空");
        }
        if (requireName && !StringUtils.hasText(body.getDeptName())) {
            throw new IllegalArgumentException("部门名称不能为空");
        }
    }

    private void fillTargetNumsIfAbsent(DeptCourseSelection body) {
        List<Integer> baseAdvancedIds = parseIdList(body.getCourseSelections());
        List<Integer> practicalIds = parseIdList(body.getPracticalSelections());
        int basic = 0;
        int advanced = 0;
        for (Integer id : baseAdvancedIds) {
            CoursePlanningInfoVO course = coursePlanningInfoMapper.getById(id);
            if (course == null || course.getCourseLevel() == null) {
                continue;
            }
            if ("基础".equals(course.getCourseLevel())) {
                basic++;
            } else if ("进阶".equals(course.getCourseLevel())) {
                advanced++;
            }
        }
        if (body.getBasicTargetCoursesNum() == null) {
            body.setBasicTargetCoursesNum(basic);
        }
        if (body.getAdvancedTargetCoursesNum() == null) {
            body.setAdvancedTargetCoursesNum(advanced);
        }
        if (body.getPracticalTargetCoursesNum() == null) {
            body.setPracticalTargetCoursesNum(practicalIds.size());
        }
    }

    private static String normalizeIdList(String raw) {
        List<Integer> ids = parseIdList(raw);
        if (ids.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(ids.get(i));
        }
        return sb.toString();
    }

    private static List<Integer> parseIdList(String raw) {
        List<Integer> ids = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return ids;
        }
        Set<Integer> seen = new HashSet<>();
        for (String part : raw.split(",")) {
            String t = part.trim();
            if (t.isEmpty()) {
                continue;
            }
            try {
                int id = Integer.parseInt(t);
                if (seen.add(id)) {
                    ids.add(id);
                }
            } catch (NumberFormatException ignored) {
                // skip invalid
            }
        }
        return ids;
    }
}
