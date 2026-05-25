package com.huawei.aitransform.service.impl;

import com.huawei.aitransform.constant.DepartmentConstants;
import com.huawei.aitransform.entity.CourseCompletionColumnVO;
import com.huawei.aitransform.entity.CourseInfoByLevelVO;
import com.huawei.aitransform.entity.DepartmentEmployeeCourseCompletionDetailVO;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.entity.DeptCourseSelection;
import com.huawei.aitransform.entity.EmployeeCourseCompletionRowVO;
import com.huawei.aitransform.entity.EmployeeTrainingInfoPO;
import com.huawei.aitransform.mapper.CoursePlanningInfoMapper;
import com.huawei.aitransform.mapper.DepartmentInfoMapper;
import com.huawei.aitransform.mapper.EmployeeTrainingInfoMapper;
import com.huawei.aitransform.mapper.PersonalCourseCompletionMapper;
import com.huawei.aitransform.service.DepartmentEmployeeCourseCompletionDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 部门全员目标课程完课矩阵（导出）
 */
@Service
public class DepartmentEmployeeCourseCompletionDetailServiceImpl implements DepartmentEmployeeCourseCompletionDetailService {

    private static final String LEVEL_BASIC = "基础";
    private static final String LEVEL_ADVANCED = "进阶";
    private static final String LEVEL_PRACTICAL = "实战";
    private static final List<String> EXPORT_LEVEL_ORDER =
            Arrays.asList(LEVEL_BASIC, LEVEL_ADVANCED, LEVEL_PRACTICAL);

    @Autowired
    private DepartmentInfoMapper departmentInfoMapper;

    @Autowired
    private EmployeeTrainingInfoMapper employeeTrainingInfoMapper;

    @Autowired
    private PersonalCourseCompletionMapper personalCourseCompletionMapper;

    @Autowired
    private CoursePlanningInfoMapper coursePlanningInfoMapper;

    @Override
    public DepartmentEmployeeCourseCompletionDetailVO getDepartmentEmployeeCourseCompletionDetail(
            String deptId, Integer personType, String aiMaturity) {
        DepartmentEmployeeCourseCompletionDetailVO result = new DepartmentEmployeeCourseCompletionDetailVO();
        result.setColumns(Collections.emptyList());
        result.setRows(Collections.emptyList());

        if (deptId == null || deptId.trim().isEmpty()) {
            return result;
        }
        String resolvedDeptId = deptId.trim();
        if ("0".equals(resolvedDeptId)) {
            resolvedDeptId = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
        }
        DepartmentInfoVO dept = departmentInfoMapper.getDepartmentByCode(resolvedDeptId);
        if (dept == null || dept.getDeptLevel() == null || dept.getDeptCode() == null) {
            return result;
        }

        List<EmployeeTrainingInfoPO> employees = employeeTrainingInfoMapper.listByDeptLevelAndCode(
                dept.getDeptLevel(), dept.getDeptCode(), personType, aiMaturity);
        if (employees == null || employees.isEmpty()) {
            return result;
        }

        List<CourseInfoByLevelVO> catalog = personalCourseCompletionMapper.getCourseInfoByLevel();
        if (catalog == null) {
            catalog = Collections.emptyList();
        }
        List<CourseInfoByLevelVO> exportCatalog = catalog.stream()
                .filter(c -> c.getCourseLevel() != null && EXPORT_LEVEL_ORDER.contains(c.getCourseLevel()))
                .collect(Collectors.toList());

        Map<String, TargetCourseIdSets> targetSetsCache = new HashMap<>();
        Set<Integer> unionTargetIds = new LinkedHashSet<>();
        for (EmployeeTrainingInfoPO po : employees) {
            String fourthCode = po.getFourthdeptcode() != null ? po.getFourthdeptcode().trim() : "";
            TargetCourseIdSets sets = targetSetsCache.computeIfAbsent(fourthCode, this::resolveTargetCourseIdSetsByDept);
            unionTargetIds.addAll(sets.resolveTargetIds(exportCatalog));
        }

        List<CourseCompletionColumnVO> columns = buildColumns(exportCatalog, unionTargetIds);
        Map<Integer, String> courseIdToKey = columns.stream()
                .collect(Collectors.toMap(CourseCompletionColumnVO::getCourseId, CourseCompletionColumnVO::getKey, (a, b) -> a));

        List<EmployeeCourseCompletionRowVO> rows = new ArrayList<>();
        for (EmployeeTrainingInfoPO po : employees) {
            rows.add(buildRow(po, columns, courseIdToKey, targetSetsCache));
        }

        result.setColumns(columns);
        result.setRows(rows);
        return result;
    }

    private List<CourseCompletionColumnVO> buildColumns(List<CourseInfoByLevelVO> exportCatalog, Set<Integer> unionTargetIds) {
        if (unionTargetIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Integer> headerCount = new HashMap<>();
        List<CourseCompletionColumnVO> columns = new ArrayList<>();
        for (String level : EXPORT_LEVEL_ORDER) {
            for (CourseInfoByLevelVO course : exportCatalog) {
                if (!level.equals(course.getCourseLevel())) {
                    continue;
                }
                Integer id = course.getId();
                if (id == null || !unionTargetIds.contains(id)) {
                    continue;
                }
                String baseHeader = level + "-" + (course.getCourseName() != null ? course.getCourseName() : "");
                int count = headerCount.merge(baseHeader, 1, Integer::sum);
                String header = count > 1 ? baseHeader + "(" + count + ")" : baseHeader;

                CourseCompletionColumnVO col = new CourseCompletionColumnVO();
                col.setKey("c_" + id);
                col.setHeader(header);
                col.setCourseLevel(level);
                col.setCourseId(id);
                columns.add(col);
            }
        }
        return columns;
    }

    private EmployeeCourseCompletionRowVO buildRow(
            EmployeeTrainingInfoPO po,
            List<CourseCompletionColumnVO> columns,
            Map<Integer, String> courseIdToKey,
            Map<String, TargetCourseIdSets> targetSetsCache) {
        String fourthCode = po.getFourthdeptcode() != null ? po.getFourthdeptcode().trim() : "";
        TargetCourseIdSets sets = targetSetsCache.computeIfAbsent(fourthCode, this::resolveTargetCourseIdSetsByDept);

        Set<Integer> basicCompleted = parseCommaSeparatedCourseIds(po.getBasicCourses());
        Set<Integer> advancedCompleted = parseCommaSeparatedCourseIds(po.getAdvancedCourses());
        Set<Integer> practicalCompleted = parseCommaSeparatedCourseIds(po.getPracticalCourses());

        Map<String, Boolean> completions = new LinkedHashMap<>();
        for (CourseCompletionColumnVO col : columns) {
            String key = col.getKey();
            Integer courseId = col.getCourseId();
            String level = col.getCourseLevel();
            boolean isTarget = sets.isTargetCourse(level, courseId);
            boolean completed = isTarget && isCompleted(level, courseId, basicCompleted, advancedCompleted, practicalCompleted);
            completions.put(key, completed);
        }

        EmployeeCourseCompletionRowVO row = new EmployeeCourseCompletionRowVO();
        row.setEmployeeNumber(po.getEmployeeNumber() != null ? po.getEmployeeNumber() : "");
        row.setName(po.getLastName() != null ? po.getLastName() : "");
        row.setFirstDept(po.getFirstdept() != null ? po.getFirstdept() : "");
        row.setSecondDept(po.getSeconddept() != null ? po.getSeconddept() : "");
        row.setThirdDept(po.getThirddept() != null ? po.getThirddept() : "");
        row.setFourthDept(po.getFourthdept() != null ? po.getFourthdept() : "");
        row.setFifthDept(po.getFifthdept() != null ? po.getFifthdept() : "");
        row.setLowestDept(po.getLowestdept() != null ? po.getLowestdept() : "");
        row.setCompletions(completions);
        return row;
    }

    private static boolean isCompleted(
            String courseLevel,
            Integer courseId,
            Set<Integer> basicCompleted,
            Set<Integer> advancedCompleted,
            Set<Integer> practicalCompleted) {
        if (courseId == null) {
            return false;
        }
        if (LEVEL_BASIC.equals(courseLevel)) {
            return basicCompleted.contains(courseId);
        }
        if (LEVEL_ADVANCED.equals(courseLevel)) {
            return advancedCompleted.contains(courseId);
        }
        if (LEVEL_PRACTICAL.equals(courseLevel)) {
            return practicalCompleted.contains(courseId);
        }
        return false;
    }

    private TargetCourseIdSets resolveTargetCourseIdSetsByDept(String fourthDeptCode) {
        if (fourthDeptCode == null || fourthDeptCode.isEmpty()) {
            return TargetCourseIdSets.allCourses();
        }
        DeptCourseSelection selection = coursePlanningInfoMapper.getDeptSelectionByDeptCode(fourthDeptCode);
        if (selection == null) {
            return TargetCourseIdSets.allCourses();
        }
        Set<Integer> baseAndAdvanced = parseCommaSeparatedCourseIds(selection.getCourseSelections());
        Set<Integer> practical = parseCommaSeparatedCourseIds(selection.getPracticalSelections());
        if (baseAndAdvanced.isEmpty() && practical.isEmpty()) {
            return TargetCourseIdSets.allCourses();
        }
        return new TargetCourseIdSets(baseAndAdvanced, practical, false);
    }

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
                // skip
            }
        }
        return ids.isEmpty() ? Collections.emptySet() : ids;
    }

    private static final class TargetCourseIdSets {
        private final boolean allCoursesTarget;
        private final Set<Integer> baseAndAdvancedTargetIds;
        private final Set<Integer> practicalTargetIds;

        private TargetCourseIdSets(Set<Integer> baseAndAdvancedTargetIds, Set<Integer> practicalTargetIds, boolean allCoursesTarget) {
            this.allCoursesTarget = allCoursesTarget;
            this.baseAndAdvancedTargetIds = baseAndAdvancedTargetIds != null ? baseAndAdvancedTargetIds : Collections.emptySet();
            this.practicalTargetIds = practicalTargetIds != null ? practicalTargetIds : Collections.emptySet();
        }

        private static TargetCourseIdSets allCourses() {
            return new TargetCourseIdSets(Collections.emptySet(), Collections.emptySet(), true);
        }

        private Set<Integer> resolveTargetIds(List<CourseInfoByLevelVO> exportCatalog) {
            if (allCoursesTarget) {
                return exportCatalog.stream()
                        .map(CourseInfoByLevelVO::getId)
                        .filter(id -> id != null)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }
            Set<Integer> ids = new LinkedHashSet<>();
            ids.addAll(baseAndAdvancedTargetIds);
            ids.addAll(practicalTargetIds);
            return ids;
        }

        private boolean isTargetCourse(String courseLevel, Integer courseId) {
            if (courseId == null) {
                return false;
            }
            if (allCoursesTarget) {
                return EXPORT_LEVEL_ORDER.contains(courseLevel);
            }
            if (LEVEL_PRACTICAL.equals(courseLevel)) {
                return practicalTargetIds.contains(courseId);
            }
            if (LEVEL_BASIC.equals(courseLevel) || LEVEL_ADVANCED.equals(courseLevel)) {
                return baseAndAdvancedTargetIds.contains(courseId);
            }
            return false;
        }
    }
}
