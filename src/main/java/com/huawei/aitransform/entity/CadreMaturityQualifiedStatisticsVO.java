package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 干部成熟度任职统计VO
 */
public class CadreMaturityQualifiedStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * AI成熟度等级（L2/L3，不包含L1）
     */
    private String maturityLevel;

    /**
     * 基数人数（该成熟度下的干部总人数）
     */
    private Integer baselineCount;

    /**
     * 通过任职人数（该成熟度下已获得AI任职的干部人数）
     */
    private Integer qualifiedCount;

    /**
     * 任职人数占比（百分比，保留4位小数）
     */
    private BigDecimal qualifiedRate;

    /**
     * 按要求AI任职人数（is_qualifications_standard=1的人数）
     */
    private Integer qualifiedByRequirementCount;

    /**
     * 按要求AI任职人数占比（百分比，保留4位小数）
     */
    private BigDecimal qualifiedByRequirementRate;

    /**
     * 该成熟度下各职位类的统计列表
     */
    private List<CadreJobCategoryQualifiedStatisticsVO> jobCategoryStatistics;

    public CadreMaturityQualifiedStatisticsVO() {
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

    public List<CadreJobCategoryQualifiedStatisticsVO> getJobCategoryStatistics() {
        return jobCategoryStatistics;
    }

    public void setJobCategoryStatistics(List<CadreJobCategoryQualifiedStatisticsVO> jobCategoryStatistics) {
        this.jobCategoryStatistics = jobCategoryStatistics;
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
}


