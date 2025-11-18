package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 部门认证统计VO（包含部门信息）
 */
public class DepartmentCertStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门ID（部门编码）
     */
    private String deptCode;

    /**
     * 部门中文名
     */
    private String deptName;

    /**
     * 基线人数（总人数）
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

    public DepartmentCertStatisticsVO() {
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
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
}

