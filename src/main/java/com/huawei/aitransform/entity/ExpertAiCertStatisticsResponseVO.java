package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 专家AI认证统计响应VO
 */
public class ExpertAiCertStatisticsResponseVO implements Serializable {

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
    private List<ExpertMaturityCertStatisticsVO> maturityStatistics;

    /**
     * 总计统计（L2+L3总计，不包含职位类明细）
     */
    private ExpertMaturityCertStatisticsVO totalStatistics;

    public ExpertAiCertStatisticsResponseVO() {
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

    public List<ExpertMaturityCertStatisticsVO> getMaturityStatistics() {
        return maturityStatistics;
    }

    public void setMaturityStatistics(List<ExpertMaturityCertStatisticsVO> maturityStatistics) {
        this.maturityStatistics = maturityStatistics;
    }

    public ExpertMaturityCertStatisticsVO getTotalStatistics() {
        return totalStatistics;
    }

    public void setTotalStatistics(ExpertMaturityCertStatisticsVO totalStatistics) {
        this.totalStatistics = totalStatistics;
    }
}

