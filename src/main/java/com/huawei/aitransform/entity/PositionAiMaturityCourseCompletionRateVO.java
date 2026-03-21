package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 专家/干部训战统计（按岗位 AI 成熟度维度，单条）
 */
public class PositionAiMaturityCourseCompletionRateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String positionAiMaturity;
    private Integer personType;
    private Integer baselineCount;
    /** 组内 basic_target_courses_num 算术平均（四舍五入为整数） */
    private Integer basicCourseCount;
    private Integer advancedCourseCount;
    private Integer practicalCourseCount;
    private Integer basicAvgCompletedCount;
    private Integer advancedAvgCompletedCount;
    private Integer practicalAvgCompletedCount;
    private Double basicAvgCompletionRate;
    private Double advancedAvgCompletionRate;
    private Double practicalAvgCompletionRate;

    public String getPositionAiMaturity() {
        return positionAiMaturity;
    }

    public void setPositionAiMaturity(String positionAiMaturity) {
        this.positionAiMaturity = positionAiMaturity;
    }

    public Integer getPersonType() {
        return personType;
    }

    public void setPersonType(Integer personType) {
        this.personType = personType;
    }

    public Integer getBaselineCount() {
        return baselineCount;
    }

    public void setBaselineCount(Integer baselineCount) {
        this.baselineCount = baselineCount;
    }

    public Integer getBasicCourseCount() {
        return basicCourseCount;
    }

    public void setBasicCourseCount(Integer basicCourseCount) {
        this.basicCourseCount = basicCourseCount;
    }

    public Integer getAdvancedCourseCount() {
        return advancedCourseCount;
    }

    public void setAdvancedCourseCount(Integer advancedCourseCount) {
        this.advancedCourseCount = advancedCourseCount;
    }

    public Integer getPracticalCourseCount() {
        return practicalCourseCount;
    }

    public void setPracticalCourseCount(Integer practicalCourseCount) {
        this.practicalCourseCount = practicalCourseCount;
    }

    public Integer getBasicAvgCompletedCount() {
        return basicAvgCompletedCount;
    }

    public void setBasicAvgCompletedCount(Integer basicAvgCompletedCount) {
        this.basicAvgCompletedCount = basicAvgCompletedCount;
    }

    public Integer getAdvancedAvgCompletedCount() {
        return advancedAvgCompletedCount;
    }

    public void setAdvancedAvgCompletedCount(Integer advancedAvgCompletedCount) {
        this.advancedAvgCompletedCount = advancedAvgCompletedCount;
    }

    public Integer getPracticalAvgCompletedCount() {
        return practicalAvgCompletedCount;
    }

    public void setPracticalAvgCompletedCount(Integer practicalAvgCompletedCount) {
        this.practicalAvgCompletedCount = practicalAvgCompletedCount;
    }

    public Double getBasicAvgCompletionRate() {
        return basicAvgCompletionRate;
    }

    public void setBasicAvgCompletionRate(Double basicAvgCompletionRate) {
        this.basicAvgCompletionRate = basicAvgCompletionRate;
    }

    public Double getAdvancedAvgCompletionRate() {
        return advancedAvgCompletionRate;
    }

    public void setAdvancedAvgCompletionRate(Double advancedAvgCompletionRate) {
        this.advancedAvgCompletionRate = advancedAvgCompletionRate;
    }

    public Double getPracticalAvgCompletionRate() {
        return practicalAvgCompletionRate;
    }

    public void setPracticalAvgCompletionRate(Double practicalAvgCompletionRate) {
        this.practicalAvgCompletionRate = practicalAvgCompletionRate;
    }
}
