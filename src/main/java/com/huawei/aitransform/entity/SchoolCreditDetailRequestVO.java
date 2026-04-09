package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * AI School学分数据明细查询请求VO
 */
public class SchoolCreditDetailRequestVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门编码（点击的部门）
     */
    private String deptCode;

    /**
     * 部门层级（1-6表示部门层级，0表示全部）
     */
    private Integer deptLevel;

    /**
     * 角色类型（0-全员，1-干部，2-专家，3-基层主管）
     */
    private Integer roleType;

    /**
     * 职位族
     */
    private String jobFamily;

    /**
     * 职位类
     */
    private String jobCategory;

    /**
     * 职位子类
     */
    private String jobSubCategory;

    /**
     * 组织AI成熟度（L1/L2/L3）
     */
    private String organizationMaturity;

    /**
     * 岗位AI成熟度（L1/L2/L3）
     */
    private String positionMaturity;

    /**
     * 查询类型（baseline-基线人数，其他可扩展）
     */
    private String queryType;

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 50;

    public SchoolCreditDetailRequestVO() {
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public Integer getDeptLevel() {
        return deptLevel;
    }

    public void setDeptLevel(Integer deptLevel) {
        this.deptLevel = deptLevel;
    }

    public Integer getRoleType() {
        return roleType;
    }

    public void setRoleType(Integer roleType) {
        this.roleType = roleType;
    }

    public String getJobFamily() {
        return jobFamily;
    }

    public void setJobFamily(String jobFamily) {
        this.jobFamily = jobFamily;
    }

    public String getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
    }

    public String getJobSubCategory() {
        return jobSubCategory;
    }

    public void setJobSubCategory(String jobSubCategory) {
        this.jobSubCategory = jobSubCategory;
    }

    public String getOrganizationMaturity() {
        return organizationMaturity;
    }

    public void setOrganizationMaturity(String organizationMaturity) {
        this.organizationMaturity = organizationMaturity;
    }

    public String getPositionMaturity() {
        return positionMaturity;
    }

    public void setPositionMaturity(String positionMaturity) {
        this.positionMaturity = positionMaturity;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public int getOffset() {
        int pn = pageNum  == null || pageNum  < 1 ? 1  : pageNum;
        int ps = pageSize == null || pageSize < 1 ? 50 : pageSize;
        return (pn - 1) * ps;
    }
}