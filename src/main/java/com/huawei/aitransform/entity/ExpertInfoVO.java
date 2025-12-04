package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 专家信息VO
 */
public class ExpertInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工工号
     */
    private String employeeNumber;

    /**
     * AI成熟度（L2/L3）
     */
    private String aiMaturity;

    /**
     * 职位族（格式：职位族-职位类-职位子类）
     */
    private String jobCategory;

    public ExpertInfoVO() {
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getAiMaturity() {
        return aiMaturity;
    }

    public void setAiMaturity(String aiMaturity) {
        this.aiMaturity = aiMaturity;
    }

    public String getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
    }
}


