package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 员工下钻查询响应VO
 */
public class EmployeeDrillDownResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工详细信息列表
     */
    private List<EmployeeDetailVO> employeeDetails;

    public EmployeeDrillDownResponseVO() {
    }

    public List<EmployeeDetailVO> getEmployeeDetails() {
        return employeeDetails;
    }

    public void setEmployeeDetails(List<EmployeeDetailVO> employeeDetails) {
        this.employeeDetails = employeeDetails;
    }
}





