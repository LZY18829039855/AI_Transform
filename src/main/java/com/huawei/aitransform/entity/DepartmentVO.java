package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 部门信息VO
 */
public class DepartmentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 部门编码/ID
     */
    private String deptCode;

    /**
     * 部门名称
     */
    private String deptName;

    public DepartmentVO() {
    }

    public DepartmentVO(String deptCode, String deptName) {
        this.deptCode = deptCode;
        this.deptName = deptName;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
}

