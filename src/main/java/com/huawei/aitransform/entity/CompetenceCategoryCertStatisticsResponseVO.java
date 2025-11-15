package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 职位类认证统计响应VO
 */
public class CompetenceCategoryCertStatisticsResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门ID（部门编码）
     */
    private String deptCode;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 各职位类统计列表
     */
    private List<CompetenceCategoryCertStatisticsVO> categoryStatistics;

    /**
     * 总计统计
     */
    private CompetenceCategoryCertStatisticsVO totalStatistics;

    public CompetenceCategoryCertStatisticsResponseVO() {
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

    public List<CompetenceCategoryCertStatisticsVO> getCategoryStatistics() {
        return categoryStatistics;
    }

    public void setCategoryStatistics(List<CompetenceCategoryCertStatisticsVO> categoryStatistics) {
        this.categoryStatistics = categoryStatistics;
    }

    public CompetenceCategoryCertStatisticsVO getTotalStatistics() {
        return totalStatistics;
    }

    public void setTotalStatistics(CompetenceCategoryCertStatisticsVO totalStatistics) {
        this.totalStatistics = totalStatistics;
    }
}

