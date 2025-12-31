package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 职位类部门统计VO（用于SQL返回结果）
 */
public class CompetenceCategoryDeptStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 职位类
     */
    private String competenceCategory;

    /**
     * 总人数
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

    public CompetenceCategoryDeptStatisticsVO() {
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
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
}

