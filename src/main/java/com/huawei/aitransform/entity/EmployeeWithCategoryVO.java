package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 员工工号和职位类VO
 */
public class EmployeeWithCategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工工号
     */
    private String employeeNumber;

    /**
     * 职位类
     */
    private String competenceCategory;

    public EmployeeWithCategoryVO() {
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getCompetenceCategory() {
        return competenceCategory;
    }

    public void setCompetenceCategory(String competenceCategory) {
        this.competenceCategory = competenceCategory;
    }
}

