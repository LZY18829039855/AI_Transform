package com.huawei.aitransform.service.impl;

import com.huawei.aitransform.constant.DepartmentConstants;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.entity.EmployeeTrainingInfoPO;
import com.huawei.aitransform.entity.PositionAiMaturityCourseCompletionRateVO;
import com.huawei.aitransform.mapper.DepartmentInfoMapper;
import com.huawei.aitransform.mapper.EmployeeTrainingInfoMapper;
import com.huawei.aitransform.service.PositionAiMaturityTrainingStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 专家/干部训战统计（按岗位 AI 成熟度）
 */
@Service
public class PositionAiMaturityTrainingStatsServiceImpl implements PositionAiMaturityTrainingStatsService {

    private static final List<String> MATURITY_ORDER = Arrays.asList("L1", "L2", "L3");

    @Autowired
    private DepartmentInfoMapper departmentInfoMapper;

    @Autowired
    private EmployeeTrainingInfoMapper employeeTrainingInfoMapper;

    @Override
    public List<PositionAiMaturityCourseCompletionRateVO> listByDeptAndPersonType(String deptId, Integer personType) {
        if (deptId == null || deptId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String resolvedDeptId = "0".equals(deptId.trim())
                ? DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE
                : deptId.trim();
        DepartmentInfoVO dept = departmentInfoMapper.getDepartmentByCode(resolvedDeptId);
        if (dept == null || dept.getDeptLevel() == null || dept.getDeptLevel().trim().isEmpty()) {
            return Collections.emptyList();
        }
        String deptLevel = dept.getDeptLevel().trim();
        List<EmployeeTrainingInfoPO> rows =
                employeeTrainingInfoMapper.listByDeptLevelAndCode(deptLevel, resolvedDeptId, personType);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<EmployeeTrainingInfoPO>> byMaturity = new HashMap<>();
        for (EmployeeTrainingInfoPO po : rows) {
            String key = maturityKey(po, personType);
            if (key == null) {
                continue;
            }
            byMaturity.computeIfAbsent(key, k -> new ArrayList<>()).add(po);
        }

        List<PositionAiMaturityCourseCompletionRateVO> out = new ArrayList<>();
        for (Map.Entry<String, List<EmployeeTrainingInfoPO>> e : byMaturity.entrySet()) {
            PositionAiMaturityCourseCompletionRateVO vo = buildGroup(e.getKey(), personType, e.getValue());
            if (vo != null) {
                out.add(vo);
            }
        }
        out.sort(this::compareMaturityVo);
        return out;
    }

    private int compareMaturityVo(PositionAiMaturityCourseCompletionRateVO a, PositionAiMaturityCourseCompletionRateVO b) {
        int ia = orderIndex(a.getPositionAiMaturity());
        int ib = orderIndex(b.getPositionAiMaturity());
        if (ia != ib) {
            return Integer.compare(ia, ib);
        }
        String sa = a.getPositionAiMaturity() != null ? a.getPositionAiMaturity() : "";
        String sb = b.getPositionAiMaturity() != null ? b.getPositionAiMaturity() : "";
        return sa.compareTo(sb);
    }

    private static int orderIndex(String maturity) {
        if (maturity == null) {
            return 10_000;
        }
        int idx = MATURITY_ORDER.indexOf(maturity.trim());
        return idx >= 0 ? idx : 1000;
    }

    private String maturityKey(EmployeeTrainingInfoPO po, Integer personType) {
        String raw;
        if (personType != null && personType == 1) {
            raw = po.getCadrePositionAiMaturity();
        } else if (personType != null && personType == 2) {
            raw = po.getExpertPositionAiMaturity();
        } else {
            return null;
        }
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        return t.isEmpty() ? null : t;
    }

    private PositionAiMaturityCourseCompletionRateVO buildGroup(String maturity, Integer personType,
                                                                 List<EmployeeTrainingInfoPO> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int n = list.size();
        long sumBasicTarget = 0;
        long sumAdvTarget = 0;
        long sumPraTarget = 0;
        long totalBasicDone = 0;
        long totalAdvDone = 0;
        long totalPraDone = 0;

        for (EmployeeTrainingInfoPO po : list) {
            sumBasicTarget += intOrZero(po.getBasicTargetCoursesNum());
            sumAdvTarget += intOrZero(po.getAdvancedTargetCoursesNum());
            sumPraTarget += intOrZero(po.getPracticalTargetCoursesNum());
            totalBasicDone += countCompletedCourses(po.getBasicCourses());
            totalAdvDone += countCompletedCourses(po.getAdvancedCourses());
            totalPraDone += countCompletedCourses(po.getPracticalCourses());
        }

        double avgBasic = n > 0 ? (double) sumBasicTarget / n : 0.0;
        double avgAdv = n > 0 ? (double) sumAdvTarget / n : 0.0;
        double avgPra = n > 0 ? (double) sumPraTarget / n : 0.0;

        PositionAiMaturityCourseCompletionRateVO vo = new PositionAiMaturityCourseCompletionRateVO();
        vo.setPositionAiMaturity(maturity);
        vo.setPersonType(personType);
        vo.setBaselineCount(n);
        vo.setBasicCourseCount(roundToInt(avgBasic));
        vo.setAdvancedCourseCount(roundToInt(avgAdv));
        vo.setPracticalCourseCount(roundToInt(avgPra));

        vo.setBasicAvgCompletedCount(avgBasic > 0 ? roundToInt((double) totalBasicDone / avgBasic) : 0);
        vo.setAdvancedAvgCompletedCount(avgAdv > 0 ? roundToInt((double) totalAdvDone / avgAdv) : 0);
        vo.setPracticalAvgCompletedCount(avgPra > 0 ? roundToInt((double) totalPraDone / avgPra) : 0);

        vo.setBasicAvgCompletionRate(ratePercent(totalBasicDone, n, avgBasic));
        vo.setAdvancedAvgCompletionRate(ratePercent(totalAdvDone, n, avgAdv));
        vo.setPracticalAvgCompletionRate(ratePercent(totalPraDone, n, avgPra));
        return vo;
    }

    private static long intOrZero(Integer v) {
        return v == null ? 0L : v.longValue();
    }

    private static double ratePercent(long totalDone, int n, double avgTarget) {
        if (n <= 0 || avgTarget <= 0) {
            return 0.0;
        }
        double denom = (double) n * avgTarget;
        if (denom <= 0) {
            return 0.0;
        }
        return round2((double) totalDone / denom * 100.0);
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

    private static int roundToInt(double value) {
        return (int) Math.round(value);
    }
}
