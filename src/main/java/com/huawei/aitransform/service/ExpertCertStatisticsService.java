package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.ExpertCertStatisticsResponseVO;
import com.huawei.aitransform.entity.ExpertCertStatisticsVO;
import com.huawei.aitransform.mapper.DepartmentInfoMapper;
import com.huawei.aitransform.mapper.ExpertCertStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 专家认证统计服务类
 */
@Service
public class ExpertCertStatisticsService {

    @Autowired
    private ExpertCertStatisticsMapper expertCertStatisticsMapper;

    @Autowired
    private DepartmentInfoMapper departmentInfoMapper;

    /**
     * 查询专家任职认证数据
     * @param deptCode 部门ID（部门编码）
     * @return 统计结果
     */
    public ExpertCertStatisticsResponseVO getExpertCertStatistics(String deptCode) {
        // 1. 查询部门信息
        com.huawei.aitransform.entity.DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
        if (deptInfo == null) {
            throw new IllegalArgumentException("部门不存在：" + deptCode);
        }

        // 2. 根据部门层级查询专家数据（联表查询证书信息）
        List<ExpertCertStatisticsVO> expertList;
        if ("3".equals(deptInfo.getDeptLevel())) {
            // 三层部门：通过部门名称查询专家信息
            expertList = expertCertStatisticsMapper.getExpertStatisticsByLevel3(deptCode, deptInfo.getDeptName());
        } else if ("4".equals(deptInfo.getDeptLevel())) {
            // 四层部门：通过部门编码查询专家信息
            expertList = expertCertStatisticsMapper.getExpertStatisticsByLevel4(deptCode);
        } else {
            throw new IllegalArgumentException("只支持查询三层或四层部门的专家数据");
        }

        // 3. 构建树形结构：按成熟度分组，每个成熟度下按职位类分组
        // 3.1 按成熟度分组统计
        Map<String, Map<String, ExpertCertStatisticsVO>> maturityMap = new HashMap<>(); // 成熟度 -> 职位类 -> 统计信息

        for (ExpertCertStatisticsVO expert : expertList) {
            String aiMaturity = expert.getAiMaturity();
            if (aiMaturity == null || aiMaturity.trim().isEmpty()) {
                aiMaturity = "未知";
            }

            String jobCategory = expert.getJobCategory() != null ? expert.getJobCategory() : "未知";
            Integer hasCert = expert.getHasCert() != null ? expert.getHasCert() : 0;

            // 获取或创建成熟度对应的职位类Map
            Map<String, ExpertCertStatisticsVO> jobCategoryMap = maturityMap.getOrDefault(aiMaturity, new HashMap<>());

            // 获取或创建职位类统计对象
            ExpertCertStatisticsVO jobCategoryStat = jobCategoryMap.getOrDefault(jobCategory, new ExpertCertStatisticsVO());
            jobCategoryStat.setAiMaturity(aiMaturity);
            jobCategoryStat.setJobCategory(jobCategory);
            
            // 累加基线人数
            if (jobCategoryStat.getBaselineCount() == null) {
                jobCategoryStat.setBaselineCount(0);
            }
            jobCategoryStat.setBaselineCount(jobCategoryStat.getBaselineCount() + 1);
            
            // 累加认证人数
            if (hasCert == 1) {
                if (jobCategoryStat.getCertCount() == null) {
                    jobCategoryStat.setCertCount(0);
                }
                jobCategoryStat.setCertCount(jobCategoryStat.getCertCount() + 1);
            }

            jobCategoryMap.put(jobCategory, jobCategoryStat);
            maturityMap.put(aiMaturity, jobCategoryMap);
        }

        // 4. 构建树形结构结果
        List<ExpertCertStatisticsVO> maturityStatistics = new ArrayList<>();

        for (Map.Entry<String, Map<String, ExpertCertStatisticsVO>> maturityEntry : maturityMap.entrySet()) {
            String aiMaturity = maturityEntry.getKey();
            Map<String, ExpertCertStatisticsVO> jobCategoryMap = maturityEntry.getValue();

            // 创建成熟度统计对象（父节点）
            ExpertCertStatisticsVO maturityStat = new ExpertCertStatisticsVO();
            maturityStat.setAiMaturity(aiMaturity);
            
            int maturityBaselineCount = 0;
            int maturityCertCount = 0;
            List<ExpertCertStatisticsVO> jobCategoryStatistics = new ArrayList<>();

            // 遍历该成熟度下的所有职位类
            for (ExpertCertStatisticsVO jobCategoryStat : jobCategoryMap.values()) {
                // 计算职位类认证率
                if (jobCategoryStat.getBaselineCount() != null && jobCategoryStat.getBaselineCount() > 0) {
                    if (jobCategoryStat.getCertCount() == null) {
                        jobCategoryStat.setCertCount(0);
                    }
                    BigDecimal rate = new BigDecimal(jobCategoryStat.getCertCount())
                            .divide(new BigDecimal(jobCategoryStat.getBaselineCount()), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal(100));
                    jobCategoryStat.setCertRate(rate);
                } else {
                    jobCategoryStat.setCertRate(BigDecimal.ZERO);
                }

                // 累加成熟度总体数据
                maturityBaselineCount += jobCategoryStat.getBaselineCount();
                maturityCertCount += jobCategoryStat.getCertCount() != null ? jobCategoryStat.getCertCount() : 0;

                // 创建职位类统计对象（只包含必要字段，清除多余字段）
                ExpertCertStatisticsVO cleanJobCategoryStat = new ExpertCertStatisticsVO();
                cleanJobCategoryStat.setAiMaturity(jobCategoryStat.getAiMaturity());
                cleanJobCategoryStat.setJobCategory(jobCategoryStat.getJobCategory());
                cleanJobCategoryStat.setBaselineCount(jobCategoryStat.getBaselineCount());
                cleanJobCategoryStat.setCertCount(jobCategoryStat.getCertCount());
                cleanJobCategoryStat.setCertRate(jobCategoryStat.getCertRate());
                // 不设置 employeeNumber、hasCert、jobCategoryStatistics

                // 添加到职位类统计列表
                jobCategoryStatistics.add(cleanJobCategoryStat);
            }

            // 设置成熟度总体统计
            maturityStat.setBaselineCount(maturityBaselineCount);
            maturityStat.setCertCount(maturityCertCount);
            if (maturityBaselineCount > 0) {
                BigDecimal maturityRate = new BigDecimal(maturityCertCount)
                        .divide(new BigDecimal(maturityBaselineCount), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                maturityStat.setCertRate(maturityRate);
            } else {
                maturityStat.setCertRate(BigDecimal.ZERO);
            }

            // 设置职位类统计列表（子节点）
            maturityStat.setJobCategoryStatistics(jobCategoryStatistics);
            // 不设置 employeeNumber、hasCert、jobCategory（成熟度级别不需要这些字段）

            maturityStatistics.add(maturityStat);
        }

        // 5. 构建返回结果
        ExpertCertStatisticsResponseVO response = new ExpertCertStatisticsResponseVO();
        response.setMaturityStatistics(maturityStatistics);

        return response;
    }

    /**
     * 获取部门信息（供调试接口使用）
     */
    public com.huawei.aitransform.entity.DepartmentInfoVO getDepartmentInfo(String deptCode) {
        return departmentInfoMapper.getDepartmentByCode(deptCode);
    }

    /**
     * 获取三层部门的专家列表（供调试接口使用）
     */
    public List<ExpertCertStatisticsVO> getExpertListByLevel3(String deptCode, String deptName) {
        return expertCertStatisticsMapper.getExpertStatisticsByLevel3(deptCode, deptName);
    }

    /**
     * 获取四层部门的专家列表（供调试接口使用）
     */
    public List<ExpertCertStatisticsVO> getExpertListByLevel4(String deptCode) {
        return expertCertStatisticsMapper.getExpertStatisticsByLevel4(deptCode);
    }
}

