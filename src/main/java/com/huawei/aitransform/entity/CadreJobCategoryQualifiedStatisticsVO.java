package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 干部职位类任职统计VO
 */
public class CadreJobCategoryQualifiedStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 职位类名称（如：软件类、系统类、研究类等）
     */
    private String jobCategory;

    /**
     * 基数人数（该职位类下的干部总人数）
     */
    private Integer baselineCount;

    /**
     * 通过任职人数（该职位类下已获得AI任职的干部人数）
     */
    private Integer qualifiedCount;

    /**
     * 任职人数占比（百分比，保留4位小数）
     */
    private BigDecimal qualifiedRate;

    public CadreJobCategoryQualifiedStatisticsVO() {
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
}

