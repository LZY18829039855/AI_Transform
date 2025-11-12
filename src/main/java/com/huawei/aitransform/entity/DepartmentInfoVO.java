package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门信息VO
 */
public class DepartmentInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门ID（部门编码）
     */
    private String deptCode;

    /**
     * 部门中文名
     */
    private String deptName;

    /**
     * 部门层级
     */
    private String deptLevel;

    /**
     * 父部门编码
     */
    private String parentDeptCode;

    /**
     * 子部门列表
     */
    private List<DepartmentInfoVO> children;

    public DepartmentInfoVO() {
        this.children = new ArrayList<>();
    }

    public DepartmentInfoVO(String deptCode, String deptName, String deptLevel) {
        this.deptCode = deptCode;
        this.deptName = deptName;
        this.deptLevel = deptLevel;
        this.children = new ArrayList<>();
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

    public String getDeptLevel() {
        return deptLevel;
    }

    public void setDeptLevel(String deptLevel) {
        this.deptLevel = deptLevel;
    }

    public String getParentDeptCode() {
        return parentDeptCode;
    }

    public void setParentDeptCode(String parentDeptCode) {
        this.parentDeptCode = parentDeptCode;
    }

    public List<DepartmentInfoVO> getChildren() {
        return children;
    }

    public void setChildren(List<DepartmentInfoVO> children) {
        this.children = children;
    }
}

