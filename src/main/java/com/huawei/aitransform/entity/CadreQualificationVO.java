package com.huawei.aitransform.entity;

/**
 * 干部任职信息VO
 * 用于存储干部的AI成熟度和最高任职级别信息
 */
public class CadreQualificationVO {
    /**
     * 干部工号
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
}

