package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 部门选课信息实体
 */
public class DeptCourseSelection implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 选课ID集合（逗号分隔）
     */
    private String courseSelections;

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

    public String getCourseSelections() {
        return courseSelections;
    }

    public void setCourseSelections(String courseSelections) {
        this.courseSelections = courseSelections;
    }
}

