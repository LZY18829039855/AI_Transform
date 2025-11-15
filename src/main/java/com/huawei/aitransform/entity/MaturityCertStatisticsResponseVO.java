package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 成熟度认证统计响应VO
 */
public class MaturityCertStatisticsResponseVO implements Serializable {

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
     * 各成熟度统计列表
     */
    private List<MaturityCertStatisticsVO> maturityStatistics;

    /**
     * 总计统计
     */
    private MaturityCertStatisticsVO totalStatistics;

    public MaturityCertStatisticsResponseVO() {
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

    public List<MaturityCertStatisticsVO> getMaturityStatistics() {
        return maturityStatistics;
    }

    public void setMaturityStatistics(List<MaturityCertStatisticsVO> maturityStatistics) {
        this.maturityStatistics = maturityStatistics;
    }

    public MaturityCertStatisticsVO getTotalStatistics() {
        return totalStatistics;
    }

    public void setTotalStatistics(MaturityCertStatisticsVO totalStatistics) {
        this.totalStatistics = totalStatistics;
    }
}

