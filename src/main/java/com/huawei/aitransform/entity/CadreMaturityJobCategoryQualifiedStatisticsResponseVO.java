package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 干部成熟度职位类任职统计响应VO
 */
public class CadreMaturityJobCategoryQualifiedStatisticsResponseVO implements Serializable {

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
     * 各成熟度统计列表（仅包含L2和L3）
     */
    private List<CadreMaturityQualifiedStatisticsVO> maturityStatistics;

    /**
     * 总计统计（不包含职位类明细）
     */
    private CadreMaturityQualifiedStatisticsVO totalStatistics;

    public CadreMaturityJobCategoryQualifiedStatisticsResponseVO() {
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

    public List<CadreMaturityQualifiedStatisticsVO> getMaturityStatistics() {
        return maturityStatistics;
    }

    public void setMaturityStatistics(List<CadreMaturityQualifiedStatisticsVO> maturityStatistics) {
        this.maturityStatistics = maturityStatistics;
    }

    public CadreMaturityQualifiedStatisticsVO getTotalStatistics() {
        return totalStatistics;
    }

    public void setTotalStatistics(CadreMaturityQualifiedStatisticsVO totalStatistics) {
        this.totalStatistics = totalStatistics;
    }
}



