package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 员工认证统计响应VO
 */
public class EmployeeCertStatisticsResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 各部门统计列表
     */
    private List<DepartmentCertStatisticsVO> departmentStatistics;

    /**
     * 总计统计
     */
    private DepartmentCertStatisticsVO totalStatistics;

    public EmployeeCertStatisticsResponseVO() {
    }

    public List<DepartmentCertStatisticsVO> getDepartmentStatistics() {
        return departmentStatistics;
    }

    public void setDepartmentStatistics(List<DepartmentCertStatisticsVO> departmentStatistics) {
        this.departmentStatistics = departmentStatistics;
    }

    public DepartmentCertStatisticsVO getTotalStatistics() {
        return totalStatistics;
    }

    public void setTotalStatistics(DepartmentCertStatisticsVO totalStatistics) {
        this.totalStatistics = totalStatistics;
    }
}

