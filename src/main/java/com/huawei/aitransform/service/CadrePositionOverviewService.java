package com.huawei.aitransform.service;

import com.huawei.aitransform.constant.DepartmentConstants;
import com.huawei.aitransform.entity.CadrePositionOverviewResponseVO;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.entity.DepartmentPositionStatisticsVO;
import com.huawei.aitransform.entity.L2L3StatisticsVO;
import com.huawei.aitransform.entity.SummaryStatisticsVO;
import com.huawei.aitransform.mapper.CadreMapper;
import com.huawei.aitransform.mapper.DepartmentInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI干部岗位概述统计服务类
 */
@Service
public class CadrePositionOverviewService {

    private static final Logger logger = LoggerFactory.getLogger(CadrePositionOverviewService.class);

    @Autowired
    private DepartmentInfoMapper departmentInfoMapper;

    @Autowired
    private CadreMapper cadreMapper;

    /**
     * 获取AI干部岗位概述统计数据
     * 
     * @return AI干部岗位概述统计响应数据
     */
    public CadrePositionOverviewResponseVO getCadrePositionOverview() {
        logger.info("开始查询AI干部岗位概述统计数据");
        
        try {
            // 1. 查询云核心网产品线（031562）下的所有三级部门
            List<DepartmentInfoVO> l3Departments = departmentInfoMapper.getLevel3DepartmentsUnderLevel2(
                    DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE);
            if (l3Departments == null) {
                l3Departments = new ArrayList<>();
            }
            logger.info("查询到{}个三级部门", l3Departments.size());
            
            // 2. 查询研发管理部（030681）下的所有四级部门
            List<DepartmentInfoVO> l4Departments = departmentInfoMapper.getLevel4DepartmentsUnderLevel3(
                    DepartmentConstants.R_D_MANAGEMENT_DEPT_CODE);
            if (l4Departments == null) {
                l4Departments = new ArrayList<>();
            }
            logger.info("查询到{}个四级部门", l4Departments.size());
            
            // 3. 构建三级部门及其下所有子部门的dept_code列表
            Map<String, List<String>> l3DeptCodeMap = new HashMap<>();
            List<String> allL3DeptCodes = new ArrayList<>();
            
            for (DepartmentInfoVO l3Dept : l3Departments) {
                String l3DeptCode = l3Dept.getDeptCode();
                List<String> deptCodes = new ArrayList<>();
                // 添加三级部门本身的dept_code
                deptCodes.add(l3DeptCode);
                
                // 查询该三级部门下的所有四级部门
                List<DepartmentInfoVO> l4Depts = departmentInfoMapper.getLevel4DepartmentsUnderLevel3(l3DeptCode);
                if (l4Depts != null) {
                    for (DepartmentInfoVO l4Dept : l4Depts) {
                        deptCodes.add(l4Dept.getDeptCode());
                        // 查询该四级部门下的所有五级部门
                        List<DepartmentInfoVO> l5Depts = departmentInfoMapper.getChildDepartments(l4Dept.getDeptCode());
                        if (l5Depts != null) {
                            for (DepartmentInfoVO l5Dept : l5Depts) {
                                if ("5".equals(l5Dept.getDeptLevel())) {
                                    deptCodes.add(l5Dept.getDeptCode());
                                }
                            }
                        }
                    }
                }
                
                // 查询该三级部门下的所有五级部门（直接子部门）
                List<DepartmentInfoVO> l5Depts = departmentInfoMapper.getChildDepartments(l3DeptCode);
                if (l5Depts != null) {
                    for (DepartmentInfoVO l5Dept : l5Depts) {
                        if ("5".equals(l5Dept.getDeptLevel())) {
                            deptCodes.add(l5Dept.getDeptCode());
                        }
                    }
                }
                
                l3DeptCodeMap.put(l3DeptCode, deptCodes);
                allL3DeptCodes.addAll(deptCodes);
            }
            
            // 4. 构建四级部门及其下所有五级部门的dept_code列表
            Map<String, List<String>> l4DeptCodeMap = new HashMap<>();
            List<String> allL4DeptCodes = new ArrayList<>();
            
            for (DepartmentInfoVO l4Dept : l4Departments) {
                String l4DeptCode = l4Dept.getDeptCode();
                List<String> deptCodes = new ArrayList<>();
                // 添加四级部门本身的dept_code
                deptCodes.add(l4DeptCode);
                
                // 查询该四级部门下的所有五级部门
                List<DepartmentInfoVO> l5Depts = departmentInfoMapper.getChildDepartments(l4DeptCode);
                if (l5Depts != null) {
                    for (DepartmentInfoVO l5Dept : l5Depts) {
                        if ("5".equals(l5Dept.getDeptLevel())) {
                            deptCodes.add(l5Dept.getDeptCode());
                        }
                    }
                }
                
                l4DeptCodeMap.put(l4DeptCode, deptCodes);
                allL4DeptCodes.addAll(deptCodes);
            }
            
            // 5. 统计各三级部门的干部岗位数据
            Map<String, DepartmentPositionStatisticsVO> l3DeptStatsMap = new LinkedHashMap<>();
            
            for (DepartmentInfoVO l3Dept : l3Departments) {
                String l3DeptCode = l3Dept.getDeptCode();
                List<String> deptCodes = l3DeptCodeMap.get(l3DeptCode);
                
                if (deptCodes == null || deptCodes.isEmpty()) {
                    continue;
                }
                
                // 统计总体数据
                List<DepartmentPositionStatisticsVO> totalStats = cadreMapper.getL3DepartmentPositionStatistics(deptCodes);
                // 统计L2数据
                List<com.huawei.aitransform.entity.DepartmentL2L3StatisticsVO> l2Stats = cadreMapper.getL3DepartmentL2Statistics(deptCodes);
                // 统计L3数据
                List<com.huawei.aitransform.entity.DepartmentL2L3StatisticsVO> l3Stats = cadreMapper.getL3DepartmentL3Statistics(deptCodes);
                
                DepartmentPositionStatisticsVO deptStat = null;
                if (totalStats != null && !totalStats.isEmpty()) {
                    deptStat = totalStats.get(0);
                } else {
                    deptStat = new DepartmentPositionStatisticsVO();
                    deptStat.setDeptCode(l3DeptCode);
                    deptStat.setDeptName(l3Dept.getDeptName());
                    deptStat.setDeptLevel("L3");
                    deptStat.setTotalPositionCount(0);
                    deptStat.setL2L3PositionCount(0);
                    deptStat.setL2L3PositionRatio(0.0);
                }
                
                // 设置L2统计数据
                L2L3StatisticsVO l2Statistics = new L2L3StatisticsVO();
                if (l2Stats != null && !l2Stats.isEmpty()) {
                    com.huawei.aitransform.entity.DepartmentL2L3StatisticsVO l2Stat = l2Stats.get(0);
                    l2Statistics.setTotalCount(l2Stat.getL2TotalCount() != null ? l2Stat.getL2TotalCount() : 0);
                    l2Statistics.setSoftwareCount(l2Stat.getL2SoftwareCount() != null ? l2Stat.getL2SoftwareCount() : 0);
                    l2Statistics.setNonSoftwareCount(l2Stat.getL2NonSoftwareCount() != null ? l2Stat.getL2NonSoftwareCount() : 0);
                } else {
                    l2Statistics.setTotalCount(0);
                    l2Statistics.setSoftwareCount(0);
                    l2Statistics.setNonSoftwareCount(0);
                }
                deptStat.setL2Statistics(l2Statistics);
                
                // 设置L3统计数据
                L2L3StatisticsVO l3Statistics = new L2L3StatisticsVO();
                if (l3Stats != null && !l3Stats.isEmpty()) {
                    com.huawei.aitransform.entity.DepartmentL2L3StatisticsVO l3Stat = l3Stats.get(0);
                    l3Statistics.setTotalCount(l3Stat.getL3TotalCount() != null ? l3Stat.getL3TotalCount() : 0);
                    l3Statistics.setSoftwareCount(l3Stat.getL3SoftwareCount() != null ? l3Stat.getL3SoftwareCount() : 0);
                    l3Statistics.setNonSoftwareCount(l3Stat.getL3NonSoftwareCount() != null ? l3Stat.getL3NonSoftwareCount() : 0);
                } else {
                    l3Statistics.setTotalCount(0);
                    l3Statistics.setSoftwareCount(0);
                    l3Statistics.setNonSoftwareCount(0);
                }
                deptStat.setL3Statistics(l3Statistics);
                
                l3DeptStatsMap.put(l3DeptCode, deptStat);
            }
            
            // 6. 统计各四级部门的干部岗位数据
            Map<String, DepartmentPositionStatisticsVO> l4DeptStatsMap = new LinkedHashMap<>();
            
            for (DepartmentInfoVO l4Dept : l4Departments) {
                String l4DeptCode = l4Dept.getDeptCode();
                List<String> deptCodes = l4DeptCodeMap.get(l4DeptCode);
                
                if (deptCodes == null || deptCodes.isEmpty()) {
                    continue;
                }
                
                // 统计总体数据
                List<DepartmentPositionStatisticsVO> totalStats = cadreMapper.getL4DepartmentPositionStatistics(deptCodes);
                // 统计L2数据
                List<com.huawei.aitransform.entity.DepartmentL2L3StatisticsVO> l2Stats = cadreMapper.getL4DepartmentL2Statistics(deptCodes);
                // 统计L3数据
                List<com.huawei.aitransform.entity.DepartmentL2L3StatisticsVO> l3Stats = cadreMapper.getL4DepartmentL3Statistics(deptCodes);
                
                DepartmentPositionStatisticsVO deptStat = null;
                if (totalStats != null && !totalStats.isEmpty()) {
                    deptStat = totalStats.get(0);
                } else {
                    deptStat = new DepartmentPositionStatisticsVO();
                    deptStat.setDeptCode(l4DeptCode);
                    deptStat.setDeptName(l4Dept.getDeptName());
                    deptStat.setDeptLevel("L4");
                    deptStat.setTotalPositionCount(0);
                    deptStat.setL2L3PositionCount(0);
                    deptStat.setL2L3PositionRatio(0.0);
                }
                
                // 设置L2统计数据
                L2L3StatisticsVO l2Statistics = new L2L3StatisticsVO();
                if (l2Stats != null && !l2Stats.isEmpty()) {
                    com.huawei.aitransform.entity.DepartmentL2L3StatisticsVO l2Stat = l2Stats.get(0);
                    l2Statistics.setTotalCount(l2Stat.getL2TotalCount() != null ? l2Stat.getL2TotalCount() : 0);
                    l2Statistics.setSoftwareCount(l2Stat.getL2SoftwareCount() != null ? l2Stat.getL2SoftwareCount() : 0);
                    l2Statistics.setNonSoftwareCount(l2Stat.getL2NonSoftwareCount() != null ? l2Stat.getL2NonSoftwareCount() : 0);
                } else {
                    l2Statistics.setTotalCount(0);
                    l2Statistics.setSoftwareCount(0);
                    l2Statistics.setNonSoftwareCount(0);
                }
                deptStat.setL2Statistics(l2Statistics);
                
                // 设置L3统计数据
                L2L3StatisticsVO l3Statistics = new L2L3StatisticsVO();
                if (l3Stats != null && !l3Stats.isEmpty()) {
                    com.huawei.aitransform.entity.DepartmentL2L3StatisticsVO l3Stat = l3Stats.get(0);
                    l3Statistics.setTotalCount(l3Stat.getL3TotalCount() != null ? l3Stat.getL3TotalCount() : 0);
                    l3Statistics.setSoftwareCount(l3Stat.getL3SoftwareCount() != null ? l3Stat.getL3SoftwareCount() : 0);
                    l3Statistics.setNonSoftwareCount(l3Stat.getL3NonSoftwareCount() != null ? l3Stat.getL3NonSoftwareCount() : 0);
                } else {
                    l3Statistics.setTotalCount(0);
                    l3Statistics.setSoftwareCount(0);
                    l3Statistics.setNonSoftwareCount(0);
                }
                deptStat.setL3Statistics(l3Statistics);
                
                l4DeptStatsMap.put(l4DeptCode, deptStat);
            }
            
            // 7. 合并部门列表（三级部门和四级部门）
            List<DepartmentPositionStatisticsVO> departmentList = new ArrayList<>();
            departmentList.addAll(l3DeptStatsMap.values());
            departmentList.addAll(l4DeptStatsMap.values());
            
            // 8. 统计汇总数据
            // 8.1 统计云核心网产品线下所有三级部门的汇总数据（不包括研发管理部下的四级部门）
            SummaryStatisticsVO l3Summary = cadreMapper.getSummaryPositionStatistics(
                    allL3DeptCodes, 
                    l4Departments.stream().map(DepartmentInfoVO::getDeptCode).collect(Collectors.toList()));
            
            // 8.2 统计研发管理部下所有四级部门的汇总数据
            SummaryStatisticsVO l4Summary = cadreMapper.getL4SummaryPositionStatistics(allL4DeptCodes);
            
            // 8.3 合并汇总数据
            SummaryStatisticsVO summary = new SummaryStatisticsVO();
            int totalPositionCount = (l3Summary != null && l3Summary.getTotalPositionCount() != null ? l3Summary.getTotalPositionCount() : 0)
                    + (l4Summary != null && l4Summary.getTotalPositionCount() != null ? l4Summary.getTotalPositionCount() : 0);
            int l2L3PositionCount = (l3Summary != null && l3Summary.getL2L3PositionCount() != null ? l3Summary.getL2L3PositionCount() : 0)
                    + (l4Summary != null && l4Summary.getL2L3PositionCount() != null ? l4Summary.getL2L3PositionCount() : 0);
            double l2L3PositionRatio = totalPositionCount > 0 ? 
                    Math.round((l2L3PositionCount * 1.0 / totalPositionCount) * 10000.0) / 10000.0 : 0.0;
            
            summary.setTotalPositionCount(totalPositionCount);
            summary.setL2L3PositionCount(l2L3PositionCount);
            summary.setL2L3PositionRatio(l2L3PositionRatio);
            
            // 8.4 统计汇总的L2和L3数据
            L2L3StatisticsVO summaryL2 = cadreMapper.getSummaryL2Statistics(
                    allL3DeptCodes, 
                    l4Departments.stream().map(DepartmentInfoVO::getDeptCode).collect(Collectors.toList()));
            L2L3StatisticsVO summaryL4L2 = cadreMapper.getL4SummaryL2Statistics(allL4DeptCodes);
            
            L2L3StatisticsVO summaryL2Statistics = new L2L3StatisticsVO();
            summaryL2Statistics.setTotalCount(
                    (summaryL2 != null && summaryL2.getTotalCount() != null ? summaryL2.getTotalCount() : 0)
                    + (summaryL4L2 != null && summaryL4L2.getTotalCount() != null ? summaryL4L2.getTotalCount() : 0));
            summaryL2Statistics.setSoftwareCount(
                    (summaryL2 != null && summaryL2.getSoftwareCount() != null ? summaryL2.getSoftwareCount() : 0)
                    + (summaryL4L2 != null && summaryL4L2.getSoftwareCount() != null ? summaryL4L2.getSoftwareCount() : 0));
            summaryL2Statistics.setNonSoftwareCount(
                    (summaryL2 != null && summaryL2.getNonSoftwareCount() != null ? summaryL2.getNonSoftwareCount() : 0)
                    + (summaryL4L2 != null && summaryL4L2.getNonSoftwareCount() != null ? summaryL4L2.getNonSoftwareCount() : 0));
            summary.setL2Statistics(summaryL2Statistics);
            
            L2L3StatisticsVO summaryL3 = cadreMapper.getSummaryL3Statistics(
                    allL3DeptCodes, 
                    l4Departments.stream().map(DepartmentInfoVO::getDeptCode).collect(Collectors.toList()));
            L2L3StatisticsVO summaryL4L3 = cadreMapper.getL4SummaryL3Statistics(allL4DeptCodes);
            
            L2L3StatisticsVO summaryL3Statistics = new L2L3StatisticsVO();
            summaryL3Statistics.setTotalCount(
                    (summaryL3 != null && summaryL3.getTotalCount() != null ? summaryL3.getTotalCount() : 0)
                    + (summaryL4L3 != null && summaryL4L3.getTotalCount() != null ? summaryL4L3.getTotalCount() : 0));
            summaryL3Statistics.setSoftwareCount(
                    (summaryL3 != null && summaryL3.getSoftwareCount() != null ? summaryL3.getSoftwareCount() : 0)
                    + (summaryL4L3 != null && summaryL4L3.getSoftwareCount() != null ? summaryL4L3.getSoftwareCount() : 0));
            summaryL3Statistics.setNonSoftwareCount(
                    (summaryL3 != null && summaryL3.getNonSoftwareCount() != null ? summaryL3.getNonSoftwareCount() : 0)
                    + (summaryL4L3 != null && summaryL4L3.getNonSoftwareCount() != null ? summaryL4L3.getNonSoftwareCount() : 0));
            summary.setL3Statistics(summaryL3Statistics);
            
            // 9. 构建响应对象
            CadrePositionOverviewResponseVO response = new CadrePositionOverviewResponseVO();
            response.setSummary(summary);
            response.setDepartmentList(departmentList);
            
            logger.info("AI干部岗位概述统计数据查询完成");
            return response;
            
        } catch (Exception e) {
            logger.error("查询AI干部岗位概述统计数据失败", e);
            throw e;
        }
    }
}

