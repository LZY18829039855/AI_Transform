package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 干部成熟度认证统计VO
 */
public class CadreMaturityCertStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * AI成熟度等级（L1/L2/L3）
     */
    private String maturityLevel;

    /**
     * 基数人数（该成熟度下的干部总人数）
     */
    private Integer baselineCount;

    /**
     * 通过认证人数（该成熟度下已通过华为研究类能力认证的干部人数）
     */
    private Integer certifiedCount;

    /**
     * 认证人数占比（百分比，保留4位小数）
     */
    private BigDecimal certRate;

    /**
     * 科目二通过人数（该成熟度下已通过科目二考试的干部人数）
     */
    private Integer subject2PassCount;

    /**
     * 科目二通过率（百分比，保留4位小数）
     */
    private BigDecimal subject2PassRate;

    /**
     * 该成熟度下各职位类的统计列表
     */
    private List<CadreJobCategoryCertStatisticsVO> jobCategoryStatistics;

    public CadreMaturityCertStatisticsVO() {
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

    public List<CadreJobCategoryCertStatisticsVO> getJobCategoryStatistics() {
        return jobCategoryStatistics;
    }

    public void setJobCategoryStatistics(List<CadreJobCategoryCertStatisticsVO> jobCategoryStatistics) {
        this.jobCategoryStatistics = jobCategoryStatistics;
    }
}

