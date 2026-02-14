package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 部门课程完成率统计项 VO（单部门）
 */
public class DepartmentCourseCompletionRateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String deptId;
    private String deptName;
    private Integer baselineCount;
    private Integer basicCourseCount;
    private Integer advancedCourseCount;
    private Integer practicalCourseCount;
    /** 基础课程平均完课人数（四舍五入为整数） */
    private Integer basicAvgCompletedCount;
    /** 进阶课程平均完课人数（四舍五入为整数） */
    private Integer advancedAvgCompletedCount;
    /** 实战课程平均完课人数（四舍五入为整数） */
    private Integer practicalAvgCompletedCount;
    private Double basicAvgCompletionRate;
    private Double advancedAvgCompletionRate;
    private Double practicalAvgCompletionRate;

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
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
