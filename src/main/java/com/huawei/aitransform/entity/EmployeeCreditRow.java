package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 员工学分行记录：工号 -> 学分（通用 2 字段行记录类型）。
 * 用于 AI 认证学分、AI 任职学分等按工号聚合的批量查询结果。
 */
public class EmployeeCreditRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private String employeeNumber;
    private BigDecimal credit;

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }
}
