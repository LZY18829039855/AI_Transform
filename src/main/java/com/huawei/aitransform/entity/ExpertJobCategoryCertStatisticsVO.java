package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 专家职位类认证统计VO
 */
public class ExpertJobCategoryCertStatisticsVO implements Serializable {

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
     * 已完成AI认证人数（该职位类下已通过华为研究类能力认证的专家人数）
     */
    private Integer certifiedCount;

    /**
     * AI认证人数占比（百分比，保留4位小数）
     */
    private BigDecimal certRate;

    public ExpertJobCategoryCertStatisticsVO() {
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

    public Integer getCertifiedCount() {
        return certifiedCount;
    }

    public void setCertifiedCount(Integer certifiedCount) {
        this.certifiedCount = certifiedCount;
    }

    public BigDecimal getCertRate() {
        return certRate;
    }

    public void setCertRate(BigDecimal certRate) {
        this.certRate = certRate;
    }
}

