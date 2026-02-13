package com.huawei.aitransform.service.impl;

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
import java.util.Collections;
import java.util.List;

/**
 * 部门全员训战总览（下钻）服务实现
 */
@Service
public class DepartmentEmployeeTrainingOverviewServiceImpl implements DepartmentEmployeeTrainingOverviewService {

    @Autowired
    private DepartmentInfoMapper departmentInfoMapper;

    @Autowired
    private EmployeeTrainingInfoMapper employeeTrainingInfoMapper;

    @Override
    public List<DepartmentEmployeeTrainingOverviewVO> getDepartmentEmployeeTrainingOverview(String deptId, Integer personType) {
        if (deptId == null || deptId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        // 当前仅处理 personType = 0
        DepartmentInfoVO dept = departmentInfoMapper.getDepartmentByCode(deptId.trim());
        if (dept == null) {
            return Collections.emptyList();
        }
        String deptLevel = dept.getDeptLevel();
        String deptCode = dept.getDeptCode();
        if (deptLevel == null || deptCode == null) {
            return Collections.emptyList();
        }
        List<EmployeeTrainingInfoPO> list = employeeTrainingInfoMapper.listByDeptLevelAndCode(deptLevel, deptCode);
        List<DepartmentEmployeeTrainingOverviewVO> result = new ArrayList<>();
        for (EmployeeTrainingInfoPO po : list) {
            DepartmentEmployeeTrainingOverviewVO vo = buildOneEmployeeVO(po);
            if (vo != null) {
                result.add(vo);
            }
        }
        return result;
    }

    private DepartmentEmployeeTrainingOverviewVO buildOneEmployeeVO(EmployeeTrainingInfoPO po) {
        int basicTarget = po.getBasicTargetCoursesNum() != null ? po.getBasicTargetCoursesNum() : 0;
        int advancedTarget = po.getAdvancedTargetCoursesNum() != null ? po.getAdvancedTargetCoursesNum() : 0;
        int basicCompleted = countCompletedCourses(po.getBasicCourses());
        int advancedCompleted = countCompletedCourses(po.getAdvancedCourses());

        double basicRate = basicTarget > 0 ? basicCompleted * 100.0 / basicTarget : 0.0;
        double advancedRate = advancedTarget > 0 ? advancedCompleted * 100.0 / advancedTarget : 0.0;
        int totalTarget = basicTarget + advancedTarget;
        int totalCompleted = basicCompleted + advancedCompleted;
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
