package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * AI干部岗位概述统计响应VO
 */
public class CadrePositionOverviewResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 汇总数据（所有三级部门以及研发管理部下面所有四级部门的整体统计）
     */
    private SummaryStatisticsVO summary;

    /**
     * 部门统计数据列表（按云核心网产品线下的三级部门和研发管理部下的四级部门组织）
     */
    private List<DepartmentPositionStatisticsVO> departmentList;

    public CadrePositionOverviewResponseVO() {
    }

    public SummaryStatisticsVO getSummary() {
        return summary;
    }

    public void setSummary(SummaryStatisticsVO summary) {
        this.summary = summary;
    }

    public List<DepartmentPositionStatisticsVO> getDepartmentList() {
        return departmentList;
    }

    public void setDepartmentList(List<DepartmentPositionStatisticsVO> departmentList) {
        this.departmentList = departmentList;
    }
}

