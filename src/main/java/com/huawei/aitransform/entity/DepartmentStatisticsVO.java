package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 部门统计数据VO（按部门维度，包含PL/TM和PM两套统计数据）
 */
public class DepartmentStatisticsVO implements Serializable {

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
     * PL/TM统计数据（PL和TM合并统计）
     */
    private StatisticsDataVO plTm;

    /**
     * PM（项目经理）统计数据（单独统计）
     */
    private StatisticsDataVO pm;

    public DepartmentStatisticsVO() {
    }

    public DepartmentStatisticsVO(String deptCode, String deptName) {
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

    public StatisticsDataVO getPlTm() {
        return plTm;
    }

    public void setPlTm(StatisticsDataVO plTm) {
        this.plTm = plTm;
    }

    public StatisticsDataVO getPm() {
        return pm;
    }

    public void setPm(StatisticsDataVO pm) {
        this.pm = pm;
    }
}

