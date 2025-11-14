package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 员工认证查询请求VO
 */
public class EmployeeCertQueryRequestVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门ID（部门编码）
     */
    private String deptCode;

    /**
     * 人员类型（0-全员，其他类型待扩展）
     */
    private Integer personType;

    public EmployeeCertQueryRequestVO() {
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
}

