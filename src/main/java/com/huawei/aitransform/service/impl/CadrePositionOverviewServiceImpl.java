package com.huawei.aitransform.service.impl;

import com.huawei.aitransform.entity.*;
import com.huawei.aitransform.mapper.CadreMapper;
import com.huawei.aitransform.mapper.DepartmentInfoMapper;
import com.huawei.aitransform.service.CadrePositionOverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 干部岗位概述统计Service实现类
 */
@Service
public class CadrePositionOverviewServiceImpl implements CadrePositionOverviewService {

    @Autowired
    private DepartmentInfoMapper departmentInfoMapper;

    @Autowired
    private CadreMapper cadreMapper;

    /**
     * 云核心网产品线部门编码
     */
    private static final String CLOUD_CORE_PRODUCT_LINE_CODE = "031562";

    /**
     * 研发管理部部门编码
     */
    private static final String R_AND_D_MANAGEMENT_DEPT_CODE = "030681";

    @Override
    public CadrePositionOverviewResponseVO getCadrePositionOverview() {
        CadrePositionOverviewResponseVO response = new CadrePositionOverviewResponseVO();
        List<DepartmentPositionStatisticsVO> departmentList = new ArrayList<>();

        // 汇总数据统计变量
        int totalSum = 0;
        int l2SoftwareSum = 0;
        int l2NonSoftwareSum = 0;
        int l3SoftwareSum = 0;
        int l3NonSoftwareSum = 0;

        // 1. 获取云核心网产品线下的所有三级部门
        List<DepartmentInfoVO> l3Depts = departmentInfoMapper.getLevel3DepartmentsUnderParent(CLOUD_CORE_PRODUCT_LINE_CODE);
        if (l3Depts != null) {
            for (DepartmentInfoVO dept : l3Depts) {
                // 统计每个三级部门的数据
                CadreStatisticsCountVO countVO = cadreMapper.getCadreStatisticsByL3DeptCode(dept.getDeptCode());
                
                if (countVO != null) {
                    DepartmentPositionStatisticsVO deptVO = createDepartmentPositionStatisticsVO(
                            dept.getDeptCode(), dept.getDeptName(), "L3", countVO);
                    
                    // 如果是研发管理部（030681），需要获取其下属的四级部门并挂载到children中
                    if (R_AND_D_MANAGEMENT_DEPT_CODE.equals(dept.getDeptCode())) {
                        List<DepartmentInfoVO> l4Depts = departmentInfoMapper.getLevel4DepartmentsUnderParent(R_AND_D_MANAGEMENT_DEPT_CODE);
                        if (l4Depts != null) {
                            List<DepartmentPositionStatisticsVO> children = new ArrayList<>();
                            for (DepartmentInfoVO l4Dept : l4Depts) {
                                CadreStatisticsCountVO l4CountVO = cadreMapper.getCadreStatisticsByL4DeptCode(l4Dept.getDeptCode());
                                if (l4CountVO != null) {
                                    DepartmentPositionStatisticsVO l4DeptVO = createDepartmentPositionStatisticsVO(
                                            l4Dept.getDeptCode(), l4Dept.getDeptName(), "L4", l4CountVO);
                                    children.add(l4DeptVO);
                                }
                            }
                            deptVO.setChildren(children);
                        }
                    }

                    departmentList.add(deptVO);
                    
                    // 累加汇总数据
                    // 只有在处理三级部门时累加，这样就覆盖了整个产品线（包括研发管理部及其下属）
                    totalSum += countVO.getTotalCount();
                    l2SoftwareSum += countVO.getL2SoftwareCount();
                    l2NonSoftwareSum += countVO.getL2NonSoftwareCount();
                    l3SoftwareSum += countVO.getL3SoftwareCount();
                    l3NonSoftwareSum += countVO.getL3NonSoftwareCount();
                }
            }
        }

        response.setDepartmentList(departmentList);

        // 3. 构建汇总数据
        SummaryStatisticsVO summary = new SummaryStatisticsVO();
        summary.setTotalPositionCount(totalSum);
        
        int l2L3Total = l2SoftwareSum + l2NonSoftwareSum + l3SoftwareSum + l3NonSoftwareSum;
        summary.setL2L3PositionCount(l2L3Total);
        
        if (totalSum > 0) {
            BigDecimal ratio = new BigDecimal(l2L3Total).divide(new BigDecimal(totalSum), 4, RoundingMode.HALF_UP);
            summary.setL2L3PositionRatio(ratio.doubleValue());
        } else {
            summary.setL2L3PositionRatio(0.0);
        }

        L2L3StatisticsVO l2Stats = new L2L3StatisticsVO();
        l2Stats.setTotalCount(l2SoftwareSum + l2NonSoftwareSum);
        l2Stats.setSoftwareCount(l2SoftwareSum);
        l2Stats.setNonSoftwareCount(l2NonSoftwareSum);
        summary.setL2Statistics(l2Stats);

        L2L3StatisticsVO l3Stats = new L2L3StatisticsVO();
        l3Stats.setTotalCount(l3SoftwareSum + l3NonSoftwareSum);
        l3Stats.setSoftwareCount(l3SoftwareSum);
        l3Stats.setNonSoftwareCount(l3NonSoftwareSum);
        summary.setL3Statistics(l3Stats);

        response.setSummary(summary);

        return response;
    }

    @Override
    public CadreAiCertOverviewResponseVO getCadreAiCertificationOverview() {
        CadreAiCertOverviewResponseVO response = new CadreAiCertOverviewResponseVO();
        List<CadreAiCertStatisticsVO> departmentList = new ArrayList<>();

        // 汇总数据统计变量
        int totalSum = 0;
        int l2L3Sum = 0;
        int l2SoftwareSum = 0;
        int l3SoftwareSum = 0;
        int nonSoftwareL2L3Sum = 0;
        int qualifiedL2L3Sum = 0;

        // 1. 获取云核心网产品线下的所有三级部门
        List<DepartmentInfoVO> l3Depts = departmentInfoMapper.getLevel3DepartmentsUnderParent(CLOUD_CORE_PRODUCT_LINE_CODE);
        if (l3Depts != null) {
            for (DepartmentInfoVO dept : l3Depts) {
                // 统计每个三级部门的数据
                CadreAiCertCountVO countVO = cadreMapper.getCadreAiCertStatisticsByL3DeptCode(dept.getDeptCode());

                if (countVO != null) {
                    CadreAiCertStatisticsVO deptVO = createCadreAiCertStatisticsVO(
                            dept.getDeptCode(), dept.getDeptName(), "L3", countVO);

                    // 如果是研发管理部（030681），需要获取其下属的四级部门并挂载到children中
                    if (R_AND_D_MANAGEMENT_DEPT_CODE.equals(dept.getDeptCode())) {
                        List<DepartmentInfoVO> l4Depts = departmentInfoMapper.getLevel4DepartmentsUnderParent(R_AND_D_MANAGEMENT_DEPT_CODE);
                        if (l4Depts != null) {
                            List<CadreAiCertStatisticsVO> children = new ArrayList<>();
                            for (DepartmentInfoVO l4Dept : l4Depts) {
                                CadreAiCertCountVO l4CountVO = cadreMapper.getCadreAiCertStatisticsByL4DeptCode(l4Dept.getDeptCode());
                                if (l4CountVO != null) {
                                    CadreAiCertStatisticsVO l4DeptVO = createCadreAiCertStatisticsVO(
                                            l4Dept.getDeptCode(), l4Dept.getDeptName(), "L4", l4CountVO);
                                    children.add(l4DeptVO);
                                }
                            }
                            deptVO.setChildren(children);
                        }
                    }

                    departmentList.add(deptVO);

                    // 累加汇总数据
                    // 只有在处理三级部门时累加，这样就覆盖了整个产品线（包括研发管理部及其下属）
                    totalSum += countVO.getTotalCadreCount();
                    l2L3Sum += countVO.getL2L3Count();
                    l2SoftwareSum += countVO.getSoftwareL2Count();
                    l3SoftwareSum += countVO.getSoftwareL3Count();
                    nonSoftwareL2L3Sum += countVO.getNonSoftwareL2L3Count();
                    qualifiedL2L3Sum += countVO.getQualifiedL2L3Count();
                }
            }
        }

        response.setDepartmentList(departmentList);

        // 3. 构建汇总数据
        CadreAiCertStatisticsVO summary = new CadreAiCertStatisticsVO();
        summary.setTotalCadreCount(totalSum);
        summary.setL2L3Count(l2L3Sum);
        summary.setSoftwareL2Count(l2SoftwareSum);
        summary.setSoftwareL3Count(l3SoftwareSum);
        summary.setNonSoftwareL2L3Count(nonSoftwareL2L3Sum);
        summary.setQualifiedL2L3Count(qualifiedL2L3Sum);

        if (totalSum > 0) {
            BigDecimal ratio = new BigDecimal(qualifiedL2L3Sum).divide(new BigDecimal(totalSum), 4, RoundingMode.HALF_UP);
            summary.setQualifiedL2L3Ratio(ratio.doubleValue());
        } else {
            summary.setQualifiedL2L3Ratio(0.0);
        }

        response.setSummary(summary);

        return response;
    }

    /**
     * 构建部门岗位统计VO
     *
     * @param deptCode  部门编码
     * @param deptName  部门名称
     * @param deptLevel 部门层级
     * @param countVO   统计数据
     * @return 部门岗位统计VO
     */
    private DepartmentPositionStatisticsVO createDepartmentPositionStatisticsVO(
            String deptCode, String deptName, String deptLevel, CadreStatisticsCountVO countVO) {
        DepartmentPositionStatisticsVO vo = new DepartmentPositionStatisticsVO();
        
        // 1. 设置部门基本信息
        vo.setDeptCode(deptCode);
        vo.setDeptName(deptName);
        vo.setDeptLevel(deptLevel);
        
        // 2. 提取统计数据
        int total = countVO.getTotalCount() != null ? countVO.getTotalCount() : 0;
        int l2Soft = countVO.getL2SoftwareCount() != null ? countVO.getL2SoftwareCount() : 0;
        int l2NonSoft = countVO.getL2NonSoftwareCount() != null ? countVO.getL2NonSoftwareCount() : 0;
        int l3Soft = countVO.getL3SoftwareCount() != null ? countVO.getL3SoftwareCount() : 0;
        int l3NonSoft = countVO.getL3NonSoftwareCount() != null ? countVO.getL3NonSoftwareCount() : 0;
        
        // 3. 设置干部总岗位数
        vo.setTotalPositionCount(total);
        
        // 4. 计算并设置L2/L3干部岗位总数及占比
        int l2L3Total = l2Soft + l2NonSoft + l3Soft + l3NonSoft;
        vo.setL2L3PositionCount(l2L3Total);
        
        if (total > 0) {
            BigDecimal ratio = new BigDecimal(l2L3Total).divide(new BigDecimal(total), 4, RoundingMode.HALF_UP);
            vo.setL2L3PositionRatio(ratio.doubleValue());
        } else {
            vo.setL2L3PositionRatio(0.0);
        }
        
        // 5. 构建并设置L2干部统计详情
        L2L3StatisticsVO l2Stats = new L2L3StatisticsVO();
        l2Stats.setTotalCount(l2Soft + l2NonSoft);
        l2Stats.setSoftwareCount(l2Soft);
        l2Stats.setNonSoftwareCount(l2NonSoft);
        vo.setL2Statistics(l2Stats);
        
        // 6. 构建并设置L3干部统计详情
        L2L3StatisticsVO l3Stats = new L2L3StatisticsVO();
        l3Stats.setTotalCount(l3Soft + l3NonSoft);
        l3Stats.setSoftwareCount(l3Soft);
        l3Stats.setNonSoftwareCount(l3NonSoft);
        vo.setL3Statistics(l3Stats);
        
        return vo;
    }

    /**
     * 构建AI任职认证统计VO
     *
     * @param deptCode  部门编码
     * @param deptName  部门名称
     * @param deptLevel 部门层级
     * @param countVO   统计数据
     * @return 统计VO
     */
    private CadreAiCertStatisticsVO createCadreAiCertStatisticsVO(
            String deptCode, String deptName, String deptLevel, CadreAiCertCountVO countVO) {
        CadreAiCertStatisticsVO vo = new CadreAiCertStatisticsVO();

        vo.setDeptCode(deptCode);
        vo.setDeptName(deptName);
        vo.setDeptLevel(deptLevel);

        int total = countVO.getTotalCadreCount() != null ? countVO.getTotalCadreCount() : 0;
        vo.setTotalCadreCount(total);
        vo.setL2L3Count(countVO.getL2L3Count() != null ? countVO.getL2L3Count() : 0);
        vo.setSoftwareL2Count(countVO.getSoftwareL2Count() != null ? countVO.getSoftwareL2Count() : 0);
        vo.setSoftwareL3Count(countVO.getSoftwareL3Count() != null ? countVO.getSoftwareL3Count() : 0);
        vo.setNonSoftwareL2L3Count(countVO.getNonSoftwareL2L3Count() != null ? countVO.getNonSoftwareL2L3Count() : 0);
        
        int qualified = countVO.getQualifiedL2L3Count() != null ? countVO.getQualifiedL2L3Count() : 0;
        vo.setQualifiedL2L3Count(qualified);

        if (total > 0) {
            BigDecimal ratio = new BigDecimal(qualified).divide(new BigDecimal(total), 4, RoundingMode.HALF_UP);
            vo.setQualifiedL2L3Ratio(ratio.doubleValue());
        } else {
            vo.setQualifiedL2L3Ratio(0.0);
        }

        return vo;
    }
}

