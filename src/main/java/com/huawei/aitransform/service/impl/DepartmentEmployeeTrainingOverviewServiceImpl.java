package com.huawei.aitransform.service.impl;

import com.huawei.aitransform.constant.DepartmentConstants;
import com.huawei.aitransform.entity.DepartmentEmployeeTrainingOverviewResponseVO;
import com.huawei.aitransform.entity.DepartmentEmployeeTrainingOverviewVO;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.entity.EmployeeTrainingInfoPO;
import com.huawei.aitransform.mapper.DepartmentInfoMapper;
import com.huawei.aitransform.mapper.EmployeeTrainingInfoMapper;
import com.huawei.aitransform.service.DepartmentEmployeeTrainingOverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 部门全员训战总览（下钻）服务实现
 */
@Service
public class DepartmentEmployeeTrainingOverviewServiceImpl implements DepartmentEmployeeTrainingOverviewService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final Set<String> ALLOWED_SORT_FIELDS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "basicCompletedCount", "advancedCompletedCount", "practicalCompletedCount")));

    @Autowired
    private DepartmentInfoMapper departmentInfoMapper;

    @Autowired
    private EmployeeTrainingInfoMapper employeeTrainingInfoMapper;

    @Override
    public List<DepartmentEmployeeTrainingOverviewVO> getDepartmentEmployeeTrainingOverview(
            String deptId, Integer personType, String aiMaturity) {
        DepartmentInfoVO dept = resolveDepartment(deptId);
        if (dept == null) {
            return Collections.emptyList();
        }
        List<EmployeeTrainingInfoPO> list = employeeTrainingInfoMapper.listByDeptLevelAndCode(
                dept.getDeptLevel(), dept.getDeptCode(), personType, aiMaturity);
        return mapToVoList(list);
    }

    @Override
    public DepartmentEmployeeTrainingOverviewResponseVO getDepartmentEmployeeTrainingOverviewPage(
            String deptId,
            Integer personType,
            String aiMaturity,
            String name,
            String employeeNumber,
            String sortField,
            String sortOrder,
            Integer pageNum,
            Integer pageSize) {
        int pn = (pageNum == null || pageNum < 1) ? DEFAULT_PAGE_NUM : pageNum;
        int ps = (pageSize == null || pageSize < 1) ? DEFAULT_PAGE_SIZE : pageSize;

        DepartmentEmployeeTrainingOverviewResponseVO response = new DepartmentEmployeeTrainingOverviewResponseVO();
        response.setPageNum(pn);
        response.setPageSize(ps);

        DepartmentInfoVO dept = resolveDepartment(deptId);
        if (dept == null) {
            response.setRecords(Collections.emptyList());
            response.setTotal(0L);
            response.setPages(0);
            return response;
        }

        String nameFilter = trimToNull(name);
        String empFilter = trimToNull(employeeNumber);
        String normalizedSortField = normalizeSortField(sortField);
        String normalizedSortOrder = normalizeSortOrder(sortOrder);
        Long total = employeeTrainingInfoMapper.countOverviewByDeptLevelAndCode(
                dept.getDeptLevel(), dept.getDeptCode(), personType, aiMaturity, nameFilter, empFilter);
        long totalCount = total == null ? 0L : total;
        response.setTotal(totalCount);
        response.setPages(ps > 0 ? (int) Math.ceil((double) totalCount / ps) : 0);

        if (totalCount <= 0) {
            response.setRecords(Collections.emptyList());
            return response;
        }

        int offset = (pn - 1) * ps;
        List<EmployeeTrainingInfoPO> list = employeeTrainingInfoMapper.listOverviewByDeptLevelAndCodePaged(
                dept.getDeptLevel(),
                dept.getDeptCode(),
                personType,
                aiMaturity,
                nameFilter,
                empFilter,
                normalizedSortField,
                normalizedSortOrder,
                offset,
                ps);
        response.setRecords(mapToVoList(list));
        return response;
    }

    private static String normalizeSortField(String sortField) {
        String field = trimToNull(sortField);
        if (field == null || !ALLOWED_SORT_FIELDS.contains(field)) {
            return null;
        }
        return field;
    }

    private static String normalizeSortOrder(String sortOrder) {
        String order = trimToNull(sortOrder);
        if (order == null) {
            return "ASC";
        }
        return "DESC".equalsIgnoreCase(order) ? "DESC" : "ASC";
    }

    private DepartmentInfoVO resolveDepartment(String deptId) {
        if (deptId == null || deptId.trim().isEmpty()) {
            return null;
        }
        String resolvedDeptId = deptId.trim();
        if ("0".equals(resolvedDeptId)) {
            resolvedDeptId = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
        }
        DepartmentInfoVO dept = departmentInfoMapper.getDepartmentByCode(resolvedDeptId);
        if (dept == null) {
            return null;
        }
        if (dept.getDeptLevel() == null || dept.getDeptCode() == null) {
            return null;
        }
        return dept;
    }

    private List<DepartmentEmployeeTrainingOverviewVO> mapToVoList(List<EmployeeTrainingInfoPO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<DepartmentEmployeeTrainingOverviewVO> result = new ArrayList<>(list.size());
        for (EmployeeTrainingInfoPO po : list) {
            DepartmentEmployeeTrainingOverviewVO vo = buildOneEmployeeVO(po);
            if (vo != null) {
                result.add(vo);
            }
        }
        return result;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private DepartmentEmployeeTrainingOverviewVO buildOneEmployeeVO(EmployeeTrainingInfoPO po) {
        int basicTarget = po.getBasicTargetCoursesNum() != null ? po.getBasicTargetCoursesNum() : 0;
        int advancedTarget = po.getAdvancedTargetCoursesNum() != null ? po.getAdvancedTargetCoursesNum() : 0;
        int practicalTarget = po.getPracticalTargetCoursesNum() != null ? po.getPracticalTargetCoursesNum() : 0;
        int basicCompleted = countCompletedCourses(po.getBasicCourses());
        int advancedCompleted = countCompletedCourses(po.getAdvancedCourses());
        int practicalCompleted = countCompletedCourses(po.getPracticalCourses());

        double basicRate = basicTarget > 0 ? basicCompleted * 100.0 / basicTarget : 0.0;
        double advancedRate = advancedTarget > 0 ? advancedCompleted * 100.0 / advancedTarget : 0.0;
        double practicalRate = practicalTarget > 0 ? practicalCompleted * 100.0 / practicalTarget : 0.0;
        int totalTarget = basicTarget + advancedTarget + practicalTarget;
        int totalCompleted = basicCompleted + advancedCompleted + practicalCompleted;
        double totalRate = totalTarget > 0 ? totalCompleted * 100.0 / totalTarget : 0.0;

        DepartmentEmployeeTrainingOverviewVO vo = new DepartmentEmployeeTrainingOverviewVO();
        vo.setName(po.getLastName() != null ? po.getLastName() : "");
        vo.setEmployeeNumber(po.getEmployeeNumber() != null ? po.getEmployeeNumber() : "");
        vo.setJobCategory(po.getJobCategory() != null ? po.getJobCategory() : "");
        vo.setJobSubcategory(po.getJobSubcategory() != null ? po.getJobSubcategory() : "");
        vo.setFirstDept(po.getFirstdept() != null ? po.getFirstdept() : "");
        vo.setSecondDept(po.getSeconddept() != null ? po.getSeconddept() : "");
        vo.setThirdDept(po.getThirddept() != null ? po.getThirddept() : "");
        vo.setFourthDept(po.getFourthdept() != null ? po.getFourthdept() : "");
        vo.setFifthDept(po.getFifthdept() != null ? po.getFifthdept() : "");
        vo.setLowestDept(po.getLowestdept() != null ? po.getLowestdept() : "");
        vo.setBasicTargetCourseCount(basicTarget);
        vo.setBasicCompletedCount(basicCompleted);
        vo.setBasicCompletionRate(round2(basicRate));
        vo.setAdvancedTargetCourseCount(advancedTarget);
        vo.setAdvancedCompletedCount(advancedCompleted);
        vo.setAdvancedCompletionRate(round2(advancedRate));
        vo.setPracticalTargetCourseCount(practicalTarget);
        vo.setPracticalCompletedCount(practicalCompleted);
        vo.setPracticalCompletionRate(round2(practicalRate));
        vo.setTotalTargetCourseCount(totalTarget);
        vo.setTotalCompletedCount(totalCompleted);
        vo.setTotalCompletionRate(round2(totalRate));
        return vo;
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
}
