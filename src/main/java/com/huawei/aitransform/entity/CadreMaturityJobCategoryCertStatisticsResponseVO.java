package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 干部成熟度职位类认证统计响应VO
 */
public class CadreMaturityJobCategoryCertStatisticsResponseVO implements Serializable {

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
    private List<CadreMaturityCertStatisticsVO> maturityStatistics;

    /**
     * 总计统计（不包含职位类明细）
     */
    private CadreMaturityCertStatisticsVO totalStatistics;

    public CadreMaturityJobCategoryCertStatisticsResponseVO() {
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

    public List<CadreMaturityCertStatisticsVO> getMaturityStatistics() {
        return maturityStatistics;
    }

    public void setMaturityStatistics(List<CadreMaturityCertStatisticsVO> maturityStatistics) {
        this.maturityStatistics = maturityStatistics;
    }

    public CadreMaturityCertStatisticsVO getTotalStatistics() {
        return totalStatistics;
    }

    public void setTotalStatistics(CadreMaturityCertStatisticsVO totalStatistics) {
        this.totalStatistics = totalStatistics;
    }
}

