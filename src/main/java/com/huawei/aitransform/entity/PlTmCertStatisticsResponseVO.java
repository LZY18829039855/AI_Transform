package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * PL/TM任职与认证统计响应VO
 */
public class PlTmCertStatisticsResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 研发管理部汇总数据
     */
    private PlTmDepartmentStatisticsVO summary;

    /**
     * 各四级部门统计数据列表
     */
    private List<PlTmDepartmentStatisticsVO> departmentList;

    public PlTmCertStatisticsResponseVO() {
    }

    public PlTmDepartmentStatisticsVO getSummary() {
        return summary;
    }

    public void setSummary(PlTmDepartmentStatisticsVO summary) {
        this.summary = summary;
    }

    public List<PlTmDepartmentStatisticsVO> getDepartmentList() {
        return departmentList;
    }

    public void setDepartmentList(List<PlTmDepartmentStatisticsVO> departmentList) {
        this.departmentList = departmentList;
    }
}

