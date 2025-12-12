package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 专家成熟度认证统计VO
 */
public class ExpertMaturityCertStatisticsVO implements Serializable {

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
     * 已完成AI认证人数（该成熟度下已通过华为研究类能力认证的专家人数）
     */
    private Integer certifiedCount;

    /**
     * AI认证人数占比（百分比，保留4位小数）
     */
    private BigDecimal certRate;

    /**
     * 该成熟度下各职位类的统计列表
     */
    private List<ExpertJobCategoryCertStatisticsVO> jobCategoryStatistics;

    public ExpertMaturityCertStatisticsVO() {
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

    public List<ExpertJobCategoryCertStatisticsVO> getJobCategoryStatistics() {
        return jobCategoryStatistics;
    }

    public void setJobCategoryStatistics(List<ExpertJobCategoryCertStatisticsVO> jobCategoryStatistics) {
        this.jobCategoryStatistics = jobCategoryStatistics;
    }
}






