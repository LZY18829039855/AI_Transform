package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 成熟度认证统计VO
 */
public class MaturityCertStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成熟度等级（L1/L2/L3）
     */
    private String maturityLevel;

    /**
     * 基线人数（总人数）
     */
    private Integer baselineCount;

    /**
     * 已认证人数
     */
    private Integer certifiedCount;

    /**
     * 认证率（百分比）
     */
    private BigDecimal certRate;

    public MaturityCertStatisticsVO() {
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
}

