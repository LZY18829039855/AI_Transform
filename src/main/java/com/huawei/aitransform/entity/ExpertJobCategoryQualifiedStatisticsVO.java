package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 专家职位类任职统计VO
 */
public class ExpertJobCategoryQualifiedStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 职位类名称（从job_category字段的中间字段提取）
     */
    private String jobCategory;

    /**
     * 基线人数（该职位类下的专家总人数）
     */
    private Integer baselineCount;

    /**
     * 已完成AI任职人数（该职位类下已获得AI任职的专家人数）
     */
    private Integer qualifiedCount;

    /**
     * AI任职人数占比（百分比，保留4位小数）
     */
    private BigDecimal qualifiedRate;

    /**
     * 按要求AI任职人数（该职位类下is_qualifications_standard=1的专家人数）
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

    public ExpertJobCategoryQualifiedStatisticsVO() {
    }

    public String getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
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
}


