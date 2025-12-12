package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 专家成熟度任职统计VO
 */
public class ExpertMaturityQualifiedStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * AI成熟度等级（L2/L3）
     */
    private String maturityLevel;

    /**
     * 基线人数（该成熟度下的专家总人数）
     */
    private Integer baselineCount;

    /**
     * 已完成AI任职人数（该成熟度下已获得AI任职的专家人数）
     */
    private Integer qualifiedCount;

    /**
     * AI任职人数占比（百分比，保留4位小数）
     */
    private BigDecimal qualifiedRate;

    /**
     * 按要求AI任职人数（该成熟度下is_qualifications_standard=1的专家人数）
     */
    private Integer qualifiedByRequirementCount;

    /**
     * 按要求AI任职人数占比（百分比，保留4位小数）
     */
    private BigDecimal qualifiedByRequirementRate;

    /**
     * 按岗位要求AI任职基线人数（与对应类别的基线人数一致，但L2非软件类为0）
     */
    private Integer baselineCountByRequirement;

    /**
     * 该成熟度下各职位类的统计列表
     */
    private List<ExpertJobCategoryQualifiedStatisticsVO> jobCategoryStatistics;

    public ExpertMaturityQualifiedStatisticsVO() {
    }

    public String getMaturityLevel() {
        return maturityLevel;
    }

    public void setMaturityLevel(String maturityLevel) {
        this.maturityLevel = maturityLevel;
    }

    public Integer getBaselineCount() {
        return baselineCount;
    }

    public void setBaselineCount(Integer baselineCount) {
        this.baselineCount = baselineCount;
    }

    public Integer getQualifiedCount() {
        return qualifiedCount;
    }

    public void setQualifiedCount(Integer qualifiedCount) {
        this.qualifiedCount = qualifiedCount;
    }

    public BigDecimal getQualifiedRate() {
        return qualifiedRate;
    }

    public void setQualifiedRate(BigDecimal qualifiedRate) {
        this.qualifiedRate = qualifiedRate;
    }

    public Integer getQualifiedByRequirementCount() {
        return qualifiedByRequirementCount;
    }

    public void setQualifiedByRequirementCount(Integer qualifiedByRequirementCount) {
        this.qualifiedByRequirementCount = qualifiedByRequirementCount;
    }

    public BigDecimal getQualifiedByRequirementRate() {
        return qualifiedByRequirementRate;
    }

    public void setQualifiedByRequirementRate(BigDecimal qualifiedByRequirementRate) {
        this.qualifiedByRequirementRate = qualifiedByRequirementRate;
    }

    public Integer getBaselineCountByRequirement() {
        return baselineCountByRequirement;
    }

    public void setBaselineCountByRequirement(Integer baselineCountByRequirement) {
        this.baselineCountByRequirement = baselineCountByRequirement;
    }

    public List<ExpertJobCategoryQualifiedStatisticsVO> getJobCategoryStatistics() {
        return jobCategoryStatistics;
    }

    public void setJobCategoryStatistics(List<ExpertJobCategoryQualifiedStatisticsVO> jobCategoryStatistics) {
        this.jobCategoryStatistics = jobCategoryStatistics;
    }
}


