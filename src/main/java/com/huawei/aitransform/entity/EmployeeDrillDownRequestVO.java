package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 员工下钻查询请求VO
 */
public class EmployeeDrillDownRequestVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门ID（部门编码）
     */
    private String deptCode;

    /**
     * 人员类型（0：全员数据）
     */
    private Integer personType;

    /**
     * 数据类型（0：基线，1：任职数据，2：认证数据）
     */
    private Integer dataType;

    public EmployeeDrillDownRequestVO() {
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public Integer getPersonType() {
        return personType;
    }

    public void setPersonType(Integer personType) {
        this.personType = personType;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }
}





