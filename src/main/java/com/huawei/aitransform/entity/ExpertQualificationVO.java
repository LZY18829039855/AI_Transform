package com.huawei.aitransform.entity;

/**
 * 专家任职信息VO
 * 用于存储专家的AI成熟度和最高任职级别信息
 */
public class ExpertQualificationVO {
    /**
     * 专家工号
     */
    private String employeeNumber;

    /**
     * AI成熟度（L2/L3）
     */
    private String aiMaturity;

    /**
     * 最高任职级别（如：8级、7级、6级、5级、4级、3级、2级、1级、初级）
     */
    private String highestQualificationLevel;

    /**
     * 职位类（如：软件类、非软件类）
     */
    private String jobCategory;

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

    public String getHighestQualificationLevel() {
        return highestQualificationLevel;
    }

    public void setHighestQualificationLevel(String highestQualificationLevel) {
        this.highestQualificationLevel = highestQualificationLevel;
    }

    public String getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
    }
}



