package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 干部职位类认证统计VO
 */
public class CadreJobCategoryCertStatisticsVO implements Serializable {

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
     * 通过认证人数（该职位类下已通过华为研究类能力认证的干部人数）
     */
    private Integer certifiedCount;

    /**
     * 认证人数占比（百分比，保留4位小数）
     */
    private BigDecimal certRate;

    /**
     * 科目二通过人数（该职位类下已通过科目二考试的干部人数）
     */
    private Integer subject2PassCount;

    /**
     * 科目二通过率（百分比，保留4位小数）
     */
    private BigDecimal subject2PassRate;

    /**
     * 按要求持证人数（根据is_cert_standard字段统计，1代表持证）
     */
    private Integer certStandardCount;

    /**
     * 按要求持证率（百分比，保留4位小数）
     */
    private BigDecimal certStandardRate;

    public CadreJobCategoryCertStatisticsVO() {
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

    public Integer getSubject2PassCount() {
        return subject2PassCount;
    }

    public void setSubject2PassCount(Integer subject2PassCount) {
        this.subject2PassCount = subject2PassCount;
    }

    public BigDecimal getSubject2PassRate() {
        return subject2PassRate;
    }

    public void setSubject2PassRate(BigDecimal subject2PassRate) {
        this.subject2PassRate = subject2PassRate;
    }

    public Integer getCertStandardCount() {
        return certStandardCount;
    }

    public void setCertStandardCount(Integer certStandardCount) {
        this.certStandardCount = certStandardCount;
    }

    public BigDecimal getCertStandardRate() {
        return certStandardRate;
    }

    public void setCertStandardRate(BigDecimal certStandardRate) {
        this.certStandardRate = certStandardRate;
    }
}



