package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 员工认证检查请求VO
 */
public class EmployeeCertCheckRequestVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工工号列表
     */
    private List<String> employeeNumbers;

    public EmployeeCertCheckRequestVO() {
    }

    public List<String> getEmployeeNumbers() {
        return employeeNumbers;
    }

    public void setEmployeeNumbers(List<String> employeeNumbers) {
        this.employeeNumbers = employeeNumbers;
    }
}

