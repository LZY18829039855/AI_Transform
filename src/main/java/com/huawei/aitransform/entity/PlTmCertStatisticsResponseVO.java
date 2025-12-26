package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * PL/TM/PM任职与认证统计响应VO
 * 按部门维度返回，每个部门包含PL/TM和PM两套统计数据
 */
public class PlTmCertStatisticsResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 研发管理部汇总数据（按部门维度，包含PL/TM和PM两套统计数据）
     */
    private DepartmentStatisticsVO summary;

    /**
     * 各四级部门统计数据列表（按部门维度，每个部门包含PL/TM和PM两套统计数据）
     */
    private List<DepartmentStatisticsVO> departmentList;

    public PlTmCertStatisticsResponseVO() {
    }

    public DepartmentStatisticsVO getSummary() {
        return summary;
    }

    public void setSummary(DepartmentStatisticsVO summary) {
        this.summary = summary;
    }

    public List<DepartmentStatisticsVO> getDepartmentList() {
        return departmentList;
    }

    public void setDepartmentList(List<DepartmentStatisticsVO> departmentList) {
        this.departmentList = departmentList;
    }
}

