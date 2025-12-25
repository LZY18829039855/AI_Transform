package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * PL/TM部门统计VO
 */
public class PlTmDepartmentStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * PL/TM总人数
     */
    private Integer totalCount;

    /**
     * 通过任职标准的人数（is_qualifications_standard=1）
     */
    private Integer qualifiedCount;

    /**
     * 任职占比（qualifiedCount/totalCount）
     */
    private Double qualifiedRatio;

    /**
     * 通过认证标准的人数（is_cert_standard=1）
     */
    private Integer certCount;

    /**
     * 认证占比（certCount/totalCount）
     */
    private Double certRatio;

    public PlTmDepartmentStatisticsVO() {
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

    public Integer getQualifiedCount() {
        return qualifiedCount;
    }

    public void setQualifiedCount(Integer qualifiedCount) {
        this.qualifiedCount = qualifiedCount;
    }

    public Double getQualifiedRatio() {
        return qualifiedRatio;
    }

    public void setQualifiedRatio(Double qualifiedRatio) {
        this.qualifiedRatio = qualifiedRatio;
    }

    public Integer getCertCount() {
        return certCount;
    }

    public void setCertCount(Integer certCount) {
        this.certCount = certCount;
    }

    public Double getCertRatio() {
        return certRatio;
    }

    public void setCertRatio(Double certRatio) {
        this.certRatio = certRatio;
    }
}

