package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 员工认证统计VO
 */
public class EmployeeCertStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总人数（基线人数）
     */
    private Integer totalCount;

    /**
     * 已认证人数
     */
    private Integer certifiedCount;

    /**
     * 认证率（百分比）
     */
    private BigDecimal certRate;

    public EmployeeCertStatisticsVO() {
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
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

