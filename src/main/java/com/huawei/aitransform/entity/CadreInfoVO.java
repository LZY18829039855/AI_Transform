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
}

