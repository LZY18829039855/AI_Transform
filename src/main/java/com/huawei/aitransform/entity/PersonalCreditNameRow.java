package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 全员学分信息表 t_personal_credit 中用于按工号解析姓名的查询结果（employee_number + last_name）。
 */
public class PersonalCreditNameRow implements Serializable {

    private static final long serialVersionUID = 1L;

    private String employeeNumber;
    /** 对应库列 last_name，作为手动录入学分时的 employee_name 来源 */
    private String lastName;

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
