package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 部门成熟度VO
 */
public class DepartmentMaturityVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * AI成熟度（L1/L2/L3）
     */
    private String aiMaturity;

    public DepartmentMaturityVO() {
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getAiMaturity() {
        return aiMaturity;
    }

    public void setAiMaturity(String aiMaturity) {
        this.aiMaturity = aiMaturity;
    }
}

