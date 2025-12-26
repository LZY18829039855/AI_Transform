package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 部门岗位统计数据VO
 */
public class DepartmentPositionStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门编码（三级部门编码或四级部门编码）
     */
    private String deptCode;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门层级（L3表示三级部门，L4表示四级部门）
     */
    private String deptLevel;

    /**
     * 该部门干部总岗位数
     */
    private Integer totalPositionCount;

    /**
     * 该部门L2/L3干部岗位总数
     */
    private Integer l2L3PositionCount;

    /**
     * 该部门L2/L3干部岗位占比
     */
    private Double l2L3PositionRatio;

    /**
     * 该部门L2干部统计数据
     */
    private L2L3StatisticsVO l2Statistics;

    /**
     * 该部门L3干部统计数据
     */
    private L2L3StatisticsVO l3Statistics;

    public DepartmentPositionStatisticsVO() {
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

    public Integer getTotalPositionCount() {
        return totalPositionCount;
    }

    public void setTotalPositionCount(Integer totalPositionCount) {
        this.totalPositionCount = totalPositionCount;
    }

    public Integer getL2L3PositionCount() {
        return l2L3PositionCount;
    }

    public void setL2L3PositionCount(Integer l2L3PositionCount) {
        this.l2L3PositionCount = l2L3PositionCount;
    }

    public Double getL2L3PositionRatio() {
        return l2L3PositionRatio;
    }

    public void setL2L3PositionRatio(Double l2L3PositionRatio) {
        this.l2L3PositionRatio = l2L3PositionRatio;
    }

    public L2L3StatisticsVO getL2Statistics() {
        return l2Statistics;
    }

    public void setL2Statistics(L2L3StatisticsVO l2Statistics) {
        this.l2Statistics = l2Statistics;
    }

    public L2L3StatisticsVO getL3Statistics() {
        return l3Statistics;
    }

    public void setL3Statistics(L2L3StatisticsVO l3Statistics) {
        this.l3Statistics = l3Statistics;
    }
}

