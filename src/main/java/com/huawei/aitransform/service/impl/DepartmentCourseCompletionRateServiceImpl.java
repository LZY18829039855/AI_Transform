package com.huawei.aitransform.service.impl;

import com.huawei.aitransform.constant.DepartmentConstants;
import com.huawei.aitransform.entity.CourseInfoByLevelVO;
import com.huawei.aitransform.entity.DepartmentCourseCompletionRateVO;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.entity.DeptCourseSelection;
import com.huawei.aitransform.entity.EmployeeTrainingInfoPO;
import com.huawei.aitransform.mapper.CoursePlanningInfoMapper;
import com.huawei.aitransform.mapper.DepartmentInfoMapper;
import com.huawei.aitransform.mapper.EmployeeTrainingInfoMapper;
import com.huawei.aitransform.mapper.PersonalCourseCompletionMapper;
import com.huawei.aitransform.service.DepartmentCourseCompletionRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门课程完成率查询服务实现
 */
@Service
public class DepartmentCourseCompletionRateServiceImpl implements DepartmentCourseCompletionRateService {

    @Autowired
    private DepartmentInfoMapper departmentInfoMapper;

    @Autowired
    private EmployeeTrainingInfoMapper employeeTrainingInfoMapper;

    @Autowired
    private CoursePlanningInfoMapper coursePlanningInfoMapper;

    @Autowired
    private PersonalCourseCompletionMapper personalCourseCompletionMapper;

    @Override
    public List<DepartmentCourseCompletionRateVO> getDepartmentCourseCompletionRate(String deptId, Integer personType) {
        if (deptId == null || deptId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        // 当前仅处理 personType = 0
        List<DepartmentInfoVO> departmentList = resolveDepartmentList(deptId.trim());
        if (departmentList == null || departmentList.isEmpty()) {
            return Collections.emptyList();
        }
        String inputDeptIdForLevel5 = null;
        if (isLevel4Input(deptId.trim(), departmentList)) {
            inputDeptIdForLevel5 = deptId.trim();
        }
        List<DepartmentCourseCompletionRateVO> result = new ArrayList<>();
        for (DepartmentInfoVO dept : departmentList) {
            DepartmentCourseCompletionRateVO vo = buildOneDeptStats(dept, inputDeptIdForLevel5);
            if (vo != null) {
                result.add(vo);
            }
        }
        return result;
    }

    /**
     * 根据入参 deptId 解析待统计部门列表。
     * 当 deptId 为 0 或云核心网二级部门时，仅查询并返回白名单内的四级部门（按固定顺序），以节省接口时间。
     */
    private List<DepartmentInfoVO> resolveDepartmentList(String deptId) {
        if ("0".equals(deptId) || DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE.equals(deptId)) {
            List<DepartmentInfoVO> list = departmentInfoMapper.getLevel4DepartmentsByCodes(
                    DepartmentConstants.COMPLETION_RATE_LEVEL4_DEPT_CODES);
            return sortByLevel4Order(list);
        }
        DepartmentInfoVO inputDept = departmentInfoMapper.getDepartmentByCode(deptId);
        if (inputDept == null) {
            return Collections.emptyList();
        }
        String level = inputDept.getDeptLevel();
        if (level == null) {
            return Collections.emptyList();
        }
        switch (level) {
            case "1":
                return departmentInfoMapper.getChildDepartments(deptId);
            case "2":
                return departmentInfoMapper.getLevel4DepartmentsUnderLevel2(deptId);
            case "3":
                return departmentInfoMapper.getChildDepartments(deptId);
            case "4":
                return departmentInfoMapper.getChildDepartments(deptId);
            case "5":
                return departmentInfoMapper.getChildDepartments(deptId);
            case "6":
                return Collections.emptyList();
            default:
                return Collections.emptyList();
        }
    }

    /**
     * 按 COMPLETION_RATE_LEVEL4_DEPT_CODES 中定义的顺序对四级部门列表排序
     */
    private List<DepartmentInfoVO> sortByLevel4Order(List<DepartmentInfoVO> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        List<String> order = DepartmentConstants.COMPLETION_RATE_LEVEL4_DEPT_CODES;
        list.sort(Comparator.comparingInt(dept -> {
            int idx = order.indexOf(dept.getDeptCode());
            return idx >= 0 ? idx : order.size();
        }));
        return list;
    }

    /**
     * 判断当前解析出的部门列表是否来自四级入参（此时子部门为五级，目标课程用入参四级）
     */
    private boolean isLevel4Input(String deptId, List<DepartmentInfoVO> departmentList) {
        if (departmentList.isEmpty()) {
            return false;
        }
        DepartmentInfoVO first = departmentList.get(0);
        return "5".equals(first.getDeptLevel());
    }

    /**
     * 构建单个部门的统计 VO；当子部门为五级时，targetCourseDeptId 为入参四级部门ID
     */
    private DepartmentCourseCompletionRateVO buildOneDeptStats(DepartmentInfoVO dept, String targetCourseDeptIdForLevel5) {
        String deptLevel = dept.getDeptLevel();
        String deptCode = dept.getDeptCode();
        if (deptLevel == null || deptCode == null) {
            return null;
        }
        List<EmployeeTrainingInfoPO> list = employeeTrainingInfoMapper.listByDeptLevelAndCode(deptLevel, deptCode);
        int baselineCount = list.size();

        String level4DeptCodeForTarget = resolveLevel4DeptCodeForTarget(dept, targetCourseDeptIdForLevel5);
        List<CourseInfoByLevelVO> targetCourses = getTargetCoursesByFourthDept(level4DeptCodeForTarget);
        Map<String, List<CourseInfoByLevelVO>> byLevel = targetCourses.stream().collect(Collectors.groupingBy(CourseInfoByLevelVO::getCourseLevel));
        int basicCourseCount = byLevel.getOrDefault("基础", Collections.emptyList()).size();
        int advancedCourseCount = byLevel.getOrDefault("进阶", Collections.emptyList()).size();
        int practicalCourseCount = byLevel.getOrDefault("实战", Collections.emptyList()).size();

        int basicTotalCompleted = 0;
        int advancedTotalCompleted = 0;
        int practicalTotalCompleted = 0;
        for (EmployeeTrainingInfoPO po : list) {
            basicTotalCompleted += countCompletedCourses(po.getBasicCourses());
            advancedTotalCompleted += countCompletedCourses(po.getAdvancedCourses());
            practicalTotalCompleted += countCompletedCourses(po.getPracticalCourses());
        }

        double basicAvgCompletedCount = basicCourseCount > 0 ? (double) basicTotalCompleted / basicCourseCount : 0.0;
        double advancedAvgCompletedCount = advancedCourseCount > 0 ? (double) advancedTotalCompleted / advancedCourseCount : 0.0;
        double practicalAvgCompletedCount = practicalCourseCount > 0 ? (double) practicalTotalCompleted / practicalCourseCount : 0.0;

        long denominatorBasic = (long) baselineCount * basicCourseCount;
        long denominatorAdvanced = (long) baselineCount * advancedCourseCount;
        long denominatorPractical = (long) baselineCount * practicalCourseCount;
        double basicAvgCompletionRate = denominatorBasic > 0 ? (double) basicTotalCompleted / denominatorBasic * 100 : 0.0;
        double advancedAvgCompletionRate = denominatorAdvanced > 0 ? (double) advancedTotalCompleted / denominatorAdvanced * 100 : 0.0;
        double practicalAvgCompletionRate = denominatorPractical > 0 ? (double) practicalTotalCompleted / denominatorPractical * 100 : 0.0;

        DepartmentCourseCompletionRateVO vo = new DepartmentCourseCompletionRateVO();
        vo.setDeptId(deptCode);
        vo.setDeptName(dept.getDeptName() != null ? dept.getDeptName() : "");
        vo.setBaselineCount(baselineCount);
        vo.setBasicCourseCount(basicCourseCount);
        vo.setAdvancedCourseCount(advancedCourseCount);
        vo.setPracticalCourseCount(practicalCourseCount);
        vo.setBasicAvgCompletedCount(roundToInt(basicAvgCompletedCount));
        vo.setAdvancedAvgCompletedCount(roundToInt(advancedAvgCompletedCount));
        vo.setPracticalAvgCompletedCount(roundToInt(practicalAvgCompletedCount));
        vo.setBasicAvgCompletionRate(round2(basicAvgCompletionRate));
        vo.setAdvancedAvgCompletionRate(round2(advancedAvgCompletionRate));
        vo.setPracticalAvgCompletionRate(round2(practicalAvgCompletionRate));
        return vo;
    }

    /**
     * 确定用于查询目标课程的四级部门编码：四级用自身；五级用入参四级；六级用父四级；一/二/三级无四级配置则用全部课程（传 null）
     */
    private String resolveLevel4DeptCodeForTarget(DepartmentInfoVO dept, String targetCourseDeptIdForLevel5) {
        String level = dept.getDeptLevel();
        String deptCode = dept.getDeptCode();
        if ("4".equals(level)) {
            return deptCode;
        }
        if ("5".equals(level) && targetCourseDeptIdForLevel5 != null) {
            return targetCourseDeptIdForLevel5;
        }
        if ("6".equals(level)) {
            return getLevel4AncestorDeptCode(deptCode);
        }
        return null;
    }

    /**
     * 根据部门编码向上查找祖先四级部门编码
     */
    private String getLevel4AncestorDeptCode(String deptCode) {
        String current = deptCode;
        while (current != null && !current.isEmpty()) {
            DepartmentInfoVO d = departmentInfoMapper.getDepartmentByCode(current);
            if (d == null) {
                return null;
            }
            if ("4".equals(d.getDeptLevel())) {
                return d.getDeptCode();
            }
            current = d.getParentDeptCode();
        }
        return null;
    }

    private List<CourseInfoByLevelVO> getTargetCoursesByFourthDept(String fourthDeptCode) {
        boolean useAllCourses = true;
        List<Integer> targetCourseIds = new ArrayList<>();
        if (fourthDeptCode != null && !fourthDeptCode.trim().isEmpty()) {
            DeptCourseSelection selection = coursePlanningInfoMapper.getDeptSelectionByDeptCode(fourthDeptCode);
            if (selection != null && selection.getCourseSelections() != null && !selection.getCourseSelections().trim().isEmpty()) {
                String[] parts = selection.getCourseSelections().split(",");
                for (String s : parts) {
                    s = s.trim();
                    if (!s.isEmpty()) {
                        try {
                            targetCourseIds.add(Integer.parseInt(s));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                if (!targetCourseIds.isEmpty()) {
                    useAllCourses = false;
                }
            }
        }
        if (useAllCourses) {
            return personalCourseCompletionMapper.getCourseInfoByLevel();
        } else {
            return personalCourseCompletionMapper.getCourseInfoByLevelAndIds(targetCourseIds);
        }
    }

    private static int countCompletedCourses(String commaSeparatedIds) {
        if (commaSeparatedIds == null || commaSeparatedIds.trim().isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String s : commaSeparatedIds.split(",")) {
            if (s != null && !s.trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /** 四舍五入为整数（用于平均完课人数字段） */
    private static int roundToInt(double value) {
        return (int) Math.round(value);
    }
}
