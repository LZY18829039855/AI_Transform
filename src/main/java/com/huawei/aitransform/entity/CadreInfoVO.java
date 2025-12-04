package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 干部信息VO（包含工号、部门、职位类）
 */
public class CadreInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工工号
     */
    private String employeeNumber;

    /**
     * 部门编码（最小部门ID）
     */
    private String deptCode;

    /**
     * 职位类
     */
    private String jobCategory;

    /**
     * AI成熟度（L1/L2/L3）
     */
    private String aiMaturity;

    /**
     * 是否按要求任职达标（1-达标，0-不达标）
     */
    private Integer isQualificationsStandard;

    /**
     * 是否按要求持证达标（1-达标，0-不达标）
     */
    private Integer isCertStandard;

    public CadreInfoVO() {
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
    }

    public String getAiMaturity() {
        return aiMaturity;
    }

    public void setAiMaturity(String aiMaturity) {
        this.aiMaturity = aiMaturity;
    }

    public Integer getIsQualificationsStandard() {
        return isQualificationsStandard;
    }

    public void setIsQualificationsStandard(Integer isQualificationsStandard) {
        this.isQualificationsStandard = isQualificationsStandard;
    }

    public Integer getIsCertStandard() {
        return isCertStandard;
    }

    public void setIsCertStandard(Integer isCertStandard) {
        this.isCertStandard = isCertStandard;
    }
}

