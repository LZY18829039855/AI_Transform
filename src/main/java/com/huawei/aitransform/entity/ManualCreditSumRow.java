package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 手工录入学分汇总行：按工号聚合 t_manual_enter_credit.credits
 */
public class ManualCreditSumRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private String employeeNumber;
    private BigDecimal totalCredits;

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public BigDecimal getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(BigDecimal totalCredits) {
        this.totalCredits = totalCredits;
    }
}

