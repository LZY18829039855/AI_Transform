package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 职位类认证统计VO
 */
public class CompetenceCategoryCertStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 职位类
     */
    private String competenceCategory;

    /**
     * 总人数（基线人数）
     */
    private Integer totalCount;

    /**
     * 已认证人数
     */
    private Integer certifiedCount;

    /**
     * 已任职人数
     */
    private Integer qualifiedCount;

    /**
     * 认证率（百分比）
     */
    private BigDecimal certRate;

    /**
     * 任职率（百分比）
     */
    private BigDecimal qualifiedRate;

    public CompetenceCategoryCertStatisticsVO() {
    }

    public String getCompetenceCategory() {
        return competenceCategory;
    }

    public void setCompetenceCategory(String competenceCategory) {
        this.competenceCategory = competenceCategory;
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

    public Integer getQualifiedCount() {
        return qualifiedCount;
    }

    public void setQualifiedCount(Integer qualifiedCount) {
        this.qualifiedCount = qualifiedCount;
    }

    public BigDecimal getCertRate() {
        return certRate;
    }

    public void setCertRate(BigDecimal certRate) {
        this.certRate = certRate;
    }

    public BigDecimal getQualifiedRate() {
        return qualifiedRate;
    }

    public void setQualifiedRate(BigDecimal qualifiedRate) {
        this.qualifiedRate = qualifiedRate;
    }
}

