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
                
                // 处理研发管理部作为三级部门时的特殊逻辑
                // 如果是研发管理部（作为三级部门），需要排除下属四级部门的数据吗？
                // 根据文档：研发管理部作为三级部门时，如果其下有四级部门数据，则三级部门级别的数据只统计l4_department_code为NULL的记录
                // 但是我们的SQL是 WHERE l3_department_code = #{deptCode}，这已经包含了所有属于该三级部门的数据
                // 而研发管理部下的四级部门数据的 l3_department_code 也是 030681
                // 因此，如果直接统计 l3_department_code = '030681'，会包含其下所有四级部门的数据
                // 文档中提到：汇总数据包括两部分：1. 云核心网产品线（031562）下所有三级部门的干部岗位（使用`l3_department_code`字段，不包括研发管理部下的四级部门数据，避免重复统计）
                // 这是一个潜在的问题点。
                // 如果研发管理部（030681）在 l3Depts 列表中，我们在这里统计了它。
                // 后面我们又会统计研发管理部下的四级部门。
                // 如果我们在这里统计了 030681，那么它包含了所有下属四级部门的数据。
                // 为了避免重复，我们需要区分。
                
                // 但是文档业务逻辑中描述：
                // 汇总数据：累加所有部门的统计数据
                // 部门列表：所有三级部门 + 研发管理部下的四级部门
                
                // 让我们看看 CadreMapper.xml 中的查询逻辑：
                // getCadreStatisticsByL3DeptCode: WHERE l3_department_code = #{deptCode}
                
                // 如果 030681 是三级部门，那么它的数据会被统计。
                // 如果我们后续还要统计 030681 下的四级部门，那么在展示上，030681 作为一个条目，其下的四级部门作为其他条目。
                // 这可能会导致混淆。通常三级部门列表应该包含 030681。
                
                // 根据文档：
                // 研发管理部（030681）作为三级部门时，如果其下有四级部门数据，则三级部门级别的数据只统计l4_department_code为NULL的记录
                
                // 所以，对于研发管理部（030681），我们需要特殊的查询逻辑吗？
                // 目前的 SQL 是统计所有 l3_department_code = deptCode 的数据。
                // 如果 deptCode 是 030681，那么它包含 l4_department_code 为空的和不为空的。
                // 如果我们把 030681 当作普通三级部门处理，那么它显示的是汇总数据。
                // 如果我们要把 030681 拆分成 "研发管理部(本部)" 和 "下属四级部门"，那么对于本部，应该只统计 l4 为空的。
                
                // 但是 Mapper 中没有提供只统计 l4 为空的接口。
                // 考虑到文档中的说明：
                // "研发管理部（030681）作为三级部门时，如果其下有四级部门数据，则三级部门级别的数据只统计l4_department_code为NULL的记录"
                // 这意味着我们需要一个特殊的查询，或者在 SQL 中处理。
                
                // 简单起见，且遵循“先查询对应的三级以及四级部门信息”的步骤，
                // 如果 l3Depts 中包含 030681，我们需要注意。
                // 通常 030681 是三级部门。
                
                // 既然 Mapper 只能统计 l3_department_code = ?，如果对于 030681 需要特殊处理，可能需要修改 Mapper 或 SQL。
                // 但当前指令是“修改接口文档”后的代码生成。
                // 文档里写了：
                // "研发管理部（030681）作为三级部门时，如果其下有四级部门数据，则三级部门级别的数据只统计l4_department_code为NULL的记录"
                
                // 让我们修改 Mapper SQL 来支持这一点，或者在 Service 层处理。
                // 由于 Mapper 已经生成，我不想改 Mapper 签名。
                // 但是 SQL 逻辑是 WHERE l3_department_code = #{deptCode}。
                
                // 如果是 030681，我们需要 WHERE l3_department_code = '030681' AND l4_department_code IS NULL。
                // 这可以通过在 Mapper 中添加一个特殊方法，或者修改 getCadreStatisticsByL3DeptCode 方法，增加一个 excludeL4 参数？
                // 或者在 SQL 中判断，如果 deptCode = '030681' 则加上 AND l4_department_code IS NULL？
                
                // 鉴于 Mapper XML 已经写入，我可以直接修改 XML 文件。
                // 让我们修改 getCadreStatisticsByL3DeptCode 的 SQL。
                
                if (countVO != null) {
                    // 如果是研发管理部，并且我们稍后会单独统计其四级部门，那么这里是否应该排除四级部门的数据？
                    // 文档说要排除。但是当前的 Mapper SQL 没有排除。
                    // 我将在代码生成后，再次修改 Mapper XML 来实现这个逻辑。
                    
                    // 暂时先按现有 Mapper 使用，稍后修复 SQL。
                    
                    DepartmentPositionStatisticsVO deptVO = createDepartmentPositionStatisticsVO(
                            dept.getDeptCode(), dept.getDeptName(), "L3", countVO);
                    departmentList.add(deptVO);
                    
                    // 累加汇总数据
                    // 注意：如果这里包含 030681 且没有排除四级部门，且后续又加了四级部门，那么汇总数据会重复。
                    // 所以必须解决这个问题。
                    
                    // 假设我稍后会修改 Mapper。
                    totalSum += countVO.getTotalCount();
                    l2SoftwareSum += countVO.getL2SoftwareCount();
                    l2NonSoftwareSum += countVO.getL2NonSoftwareCount();
                    l3SoftwareSum += countVO.getL3SoftwareCount();
                    l3NonSoftwareSum += countVO.getL3NonSoftwareCount();
                }
            }
        }

        // 2. 获取研发管理部下的所有四级部门
        List<DepartmentInfoVO> l4Depts = departmentInfoMapper.getLevel4DepartmentsUnderParent(R_AND_D_MANAGEMENT_DEPT_CODE);
        if (l4Depts != null) {
            for (DepartmentInfoVO dept : l4Depts) {
                CadreStatisticsCountVO countVO = cadreMapper.getCadreStatisticsByL4DeptCode(dept.getDeptCode());
                if (countVO != null) {
                    DepartmentPositionStatisticsVO deptVO = createDepartmentPositionStatisticsVO(
                            dept.getDeptCode(), dept.getDeptName(), "L4", countVO);
                    departmentList.add(deptVO);
                    
                    // 累加汇总数据
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

    private DepartmentPositionStatisticsVO createDepartmentPositionStatisticsVO(
            String deptCode, String deptName, String deptLevel, CadreStatisticsCountVO countVO) {
        DepartmentPositionStatisticsVO vo = new DepartmentPositionStatisticsVO();
        vo.setDeptCode(deptCode);
        vo.setDeptName(deptName);
        vo.setDeptLevel(deptLevel);
        
        int total = countVO.getTotalCount() != null ? countVO.getTotalCount() : 0;
        vo.setTotalPositionCount(total);
        
        int l2Soft = countVO.getL2SoftwareCount() != null ? countVO.getL2SoftwareCount() : 0;
        int l2NonSoft = countVO.getL2NonSoftwareCount() != null ? countVO.getL2NonSoftwareCount() : 0;
        int l3Soft = countVO.getL3SoftwareCount() != null ? countVO.getL3SoftwareCount() : 0;
        int l3NonSoft = countVO.getL3NonSoftwareCount() != null ? countVO.getL3NonSoftwareCount() : 0;
        
        int l2L3Total = l2Soft + l2NonSoft + l3Soft + l3NonSoft;
        vo.setL2L3PositionCount(l2L3Total);
        
        if (total > 0) {
            BigDecimal ratio = new BigDecimal(l2L3Total).divide(new BigDecimal(total), 4, RoundingMode.HALF_UP);
            vo.setL2L3PositionRatio(ratio.doubleValue());
        } else {
            vo.setL2L3PositionRatio(0.0);
        }
        
        L2L3StatisticsVO l2Stats = new L2L3StatisticsVO();
        l2Stats.setTotalCount(l2Soft + l2NonSoft);
        l2Stats.setSoftwareCount(l2Soft);
        l2Stats.setNonSoftwareCount(l2NonSoft);
        vo.setL2Statistics(l2Stats);
        
        L2L3StatisticsVO l3Stats = new L2L3StatisticsVO();
        l3Stats.setTotalCount(l3Soft + l3NonSoft);
        l3Stats.setSoftwareCount(l3Soft);
        l3Stats.setNonSoftwareCount(l3NonSoft);
        vo.setL3Statistics(l3Stats);
        
        return vo;
    }
}

