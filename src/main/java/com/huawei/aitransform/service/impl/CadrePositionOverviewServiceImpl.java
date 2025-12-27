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
                    // 如果是研发管理部（030681），需要排除下属四级部门的数据
                    // 对于研发管理部，汇总数据应该由两部分组成：
                    // 1. 研发管理部本部的数据（l4_department_code IS NULL） -> 这部分作为三级部门展示
                    // 2. 研发管理部下属四级部门的数据 -> 这部分作为四级部门展示
                    
                    // 但是当前的 getCadreStatisticsByL3DeptCode 查询是 WHERE l3_department_code = #{deptCode}
                    // 这会包含该三级部门下的所有数据（包括已分配到四级部门的数据）
                    
                    // 如果我们想要让 研发管理部（作为三级部门列表中的一项）只显示未分配到四级部门的人员：
                    // 那么我们需要修改查询逻辑或者在这里做减法。
                    // 但是，如果业务含义是“三级部门统计该部门下所有人”，那么应该包含四级部门的人。
                    // 可是，如果列表中同时展示了“研发管理部”和“研发管理部下的四级部门”，
                    // 那么用户看列表时，可能会把它们加起来，导致重复计算。
                    // 通常做法：父级部门行只显示直属（未下钻）的人员，或者父级部门行显示汇总（此时不应该和子级部门简单累加）。
                    
                    // 根据之前的修改，Mapper 中的 getCadreStatisticsByL3DeptCode 已经去掉了对 030681 的特殊过滤。
                    // 这意味着 countVO 包含了 030681 下的所有人（包括四级部门的人）。
                    
                    // 如果我们希望列表中的“研发管理部”行只显示直属人员（不含四级部门人员），
                    // 那么我们需要用 总数 - 四级部门总数。
                    // 或者，修改 Mapper，对于 030681 加上 AND l4_department_code IS NULL。
                    // 用户刚才说：“对于研发管理部本身，只统计那些未归属到具体四级部门（即 L4 部门为空）的干部。这里不对，统计三级部门下面的干部数据时，直接使用下面的过滤条件即可：WHERE l3_department_code = #{deptCode}，不需要对四级部门的ID做过滤限制”
                    
                    // 理解用户的意思：
                    // 用户认为之前的“只统计 L4 为空”是错误的。
                    // 用户希望“直接使用 WHERE l3_department_code = #{deptCode}”。
                    // 这意味着：在统计三级部门（包括研发管理部）时，包含其下所有四级部门的数据。
                    
                    // 那么，问题来了：如果列表中有“研发管理部”（包含所有人），又有“研发管理部下属四级部门”，
                    // 那么汇总 totalSum 时，如果简单累加 departmentList 中的所有项，就会重复计算。
                    
                    // 让我们检查汇总逻辑。
                    // totalSum 是在遍历过程中累加的。
                    
                    // 策略调整：
                    // 1. 三级部门列表（l3Depts）中包含研发管理部。
                    // 2. 我们遍历 l3Depts，计算 countVO（包含其下所有四级部门）。
                    // 3. 将其加入 departmentList。
                    // 4. 累加到 totalSum。
                    // 5. 然后，我们遍历研发管理部下的四级部门（l4Depts）。
                    // 6. 计算 countVO。
                    // 7. 将其加入 departmentList。
                    // 8. 累加到 totalSum。
                    
                    // 显然，这样 totalSum 会重复计算研发管理部下四级部门的人员。
                    // 因为这些人员既在 l3_department_code='030681' 中统计了一次，又在 l4_department_code='xxxx' 中统计了一次。
                    
                    // 修正汇总逻辑：
                    // 只有当部门不是研发管理部（030681）时，才将其 countVO 累加到 totalSum。
                    // 对于研发管理部，我们不累加它的 countVO 到 totalSum（因为我们稍后会通过累加其下的四级部门 + 本部直属人员来计算？或者直接取研发管理部的 countVO 作为该部门的总数？）
                    
                    // 另一种理解：
                    // 用户说“统计三级部门...直接使用 WHERE l3_department_code = #{deptCode}”。
                    // 这可能只是指接口返回的列表中的每一行的统计口径。
                    // 至于 Summary（汇总数据），应该由所有不重叠的部分组成。
                    // 即：所有非研发管理部的三级部门 + 研发管理部。
                    // 或者：所有非研发管理部的三级部门 + 研发管理部下的四级部门 + 研发管理部直属（无四级部门）。
                    
                    // 如果用户的意图是：列表展示归列表展示，汇总归汇总。
                    // 列表展示：
                    // - 研发管理部：显示所有（含四级）。
                    // - 研发管理部下某四级部门：显示该四级部门。
                    // 汇总数据：
                    // - 应该是整个云核心网产品线的数据。
                    // - 即：所有三级部门（包括研发管理部）的总和。
                    // - 因为研发管理部的数据已经包含了其下四级部门的数据。
                    // - 所以，只要把所有 l3Depts 的数据累加，就得到了总数。
                    // - 此时，不需要再累加 l4Depts 的数据到 totalSum。
                    
                    // 但是，l4Depts 的数据确实添加到了 departmentList 中供前端展示。
                    
                    // 所以，正确的逻辑应该是：
                    // 遍历 l3Depts：
                    //   - 计算 countVO (包含所有)。
                    //   - 加入 departmentList。
                    //   - 累加 totalSum。
                    // 遍历 l4Depts：
                    //   - 计算 countVO。
                    //   - 加入 departmentList。
                    //   - **不**累加 totalSum（因为已经在对应的三级部门中累加过了）。
                    
                    // 这样 Summary 是正确的（基于所有三级部门的全集）。
                    // 列表也是用户想要的（三级部门包含所有）。
                    
                    DepartmentPositionStatisticsVO deptVO = createDepartmentPositionStatisticsVO(
                            dept.getDeptCode(), dept.getDeptName(), "L3", countVO);
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

        // 2. 获取研发管理部下的所有四级部门
        List<DepartmentInfoVO> l4Depts = departmentInfoMapper.getLevel4DepartmentsUnderParent(R_AND_D_MANAGEMENT_DEPT_CODE);
        if (l4Depts != null) {
            for (DepartmentInfoVO dept : l4Depts) {
                CadreStatisticsCountVO countVO = cadreMapper.getCadreStatisticsByL4DeptCode(dept.getDeptCode());
                if (countVO != null) {
                    DepartmentPositionStatisticsVO deptVO = createDepartmentPositionStatisticsVO(
                            dept.getDeptCode(), dept.getDeptName(), "L4", countVO);
                    departmentList.add(deptVO);
                    
                    // 这里不再累加到汇总数据，因为这些数据已经包含在“研发管理部”这个三级部门的统计中了
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

