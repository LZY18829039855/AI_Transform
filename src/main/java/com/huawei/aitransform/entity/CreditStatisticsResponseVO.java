package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 学分总览统计响应VO
 */
public class CreditStatisticsResponseVO implements Serializable {
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
     * 各分类统计列表
     */
    private List<CreditOverviewVO> statistics;

    /**
     * 总计统计
     */
    private CreditOverviewVO totalStatistics;

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

    public List<CreditOverviewVO> getStatistics() {
        return statistics;
    }

    public void setStatistics(List<CreditOverviewVO> statistics) {
        this.statistics = statistics;
    }

    public CreditOverviewVO getTotalStatistics() {
        return totalStatistics;
    }

    public void setTotalStatistics(CreditOverviewVO totalStatistics) {
        this.totalStatistics = totalStatistics;
    }
}
