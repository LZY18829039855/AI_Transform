package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 部门L2/L3统计数据VO（用于Mapper返回结果）
 */
public class DepartmentL2L3StatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * L2/L3总数
     */
    private Integer l2TotalCount;

    /**
     * L2软件类数量
     */
    private Integer l2SoftwareCount;

    /**
     * L2非软件类数量
     */
    private Integer l2NonSoftwareCount;

    /**
     * L3总数
     */
    private Integer l3TotalCount;

    /**
     * L3软件类数量
     */
    private Integer l3SoftwareCount;

    /**
     * L3非软件类数量
     */
    private Integer l3NonSoftwareCount;

    public DepartmentL2L3StatisticsVO() {
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public Integer getL2TotalCount() {
        return l2TotalCount;
    }

    public void setL2TotalCount(Integer l2TotalCount) {
        this.l2TotalCount = l2TotalCount;
    }

    public Integer getL2SoftwareCount() {
        return l2SoftwareCount;
    }

    public void setL2SoftwareCount(Integer l2SoftwareCount) {
        this.l2SoftwareCount = l2SoftwareCount;
    }

    public Integer getL2NonSoftwareCount() {
        return l2NonSoftwareCount;
    }

    public void setL2NonSoftwareCount(Integer l2NonSoftwareCount) {
        this.l2NonSoftwareCount = l2NonSoftwareCount;
    }

    public Integer getL3TotalCount() {
        return l3TotalCount;
    }

    public void setL3TotalCount(Integer l3TotalCount) {
        this.l3TotalCount = l3TotalCount;
    }

    public Integer getL3SoftwareCount() {
        return l3SoftwareCount;
    }

    public void setL3SoftwareCount(Integer l3SoftwareCount) {
        this.l3SoftwareCount = l3SoftwareCount;
    }

    public Integer getL3NonSoftwareCount() {
        return l3NonSoftwareCount;
    }

    public void setL3NonSoftwareCount(Integer l3NonSoftwareCount) {
        this.l3NonSoftwareCount = l3NonSoftwareCount;
    }
}

