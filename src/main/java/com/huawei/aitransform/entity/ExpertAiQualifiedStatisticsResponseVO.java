package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 专家AI任职统计响应VO
 */
public class ExpertAiQualifiedStatisticsResponseVO implements Serializable {

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
     * 各成熟度统计列表（L2、L3）
     */
    private List<ExpertMaturityQualifiedStatisticsVO> maturityStatistics;

    /**
     * 总计统计（L2+L3总计，不包含职位类明细）
     */
    private ExpertMaturityQualifiedStatisticsVO totalStatistics;

    public ExpertAiQualifiedStatisticsResponseVO() {
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

    public List<ExpertMaturityQualifiedStatisticsVO> getMaturityStatistics() {
        return maturityStatistics;
    }

    public void setMaturityStatistics(List<ExpertMaturityQualifiedStatisticsVO> maturityStatistics) {
        this.maturityStatistics = maturityStatistics;
    }

    public ExpertMaturityQualifiedStatisticsVO getTotalStatistics() {
        return totalStatistics;
    }

    public void setTotalStatistics(ExpertMaturityQualifiedStatisticsVO totalStatistics) {
        this.totalStatistics = totalStatistics;
    }
}

