package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 学分总览统计视图对象
 */
public class CreditOverviewVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 类别名称（部门名称或职位类别名称）
     */
    private String categoryName;

    /**
     * 类别编码（部门编码或职位类别编码，用于下钻筛选）
     */
    private String categoryCode;

    /**
     * 分组维度对应的部门层级（1-6 对应 first~sixth 部门编码列；-1 表示按最小部门编号 lowest_dept_number 分组）
     */
    private Integer categoryLevel;

    /**
     * 基线人数
     */
    private Integer baselineHeadcount;

    /**
     * 个人最高分
     */
    private BigDecimal maxScore;

    /**
     * 个人最低分
     */
    private BigDecimal minScore;

    /**
     * 当前平均学分
     */
    private BigDecimal averageCurrentCredit;

    /**
     * 目标平均学分
     */
    private BigDecimal averageTargetCredit;

    /**
     * 学分达成率
     */
    private BigDecimal achievementRate;

    /**
     * 时间进度（百分比，兼容旧字段）
     */
    private BigDecimal timeProgress;

    /**
     * 时间进度学分目标（目标平均学分 × 当年已过天数/全年天数）
     */
    private BigDecimal scheduleTarget;

    /**
     * 学分状态预警（正常、轻度预警、滞后预警）
     * 规则：当前平均学分 >= 时间进度学分目标 → 正常；
     * 当前平均学分 >= 时间进度学分目标×80% → 轻度预警；否则滞后预警
     */
    private String status;

    /**
     * 状态类型（success、warning、danger）
     */
    private String statusType;

    /**
     * 是否预警（非“正常”即为 true，兼容旧字段）
     */
    private Boolean isWarning;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public Integer getCategoryLevel() {
        return categoryLevel;
    }

    public void setCategoryLevel(Integer categoryLevel) {
        this.categoryLevel = categoryLevel;
    }

    public Integer getBaselineHeadcount() {
        return baselineHeadcount;
    }

    public void setBaselineHeadcount(Integer baselineHeadcount) {
        this.baselineHeadcount = baselineHeadcount;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }

    public BigDecimal getMinScore() {
        return minScore;
    }

    public void setMinScore(BigDecimal minScore) {
        this.minScore = minScore;
    }

    public BigDecimal getAverageCurrentCredit() {
        return averageCurrentCredit;
    }

    public void setAverageCurrentCredit(BigDecimal averageCurrentCredit) {
        this.averageCurrentCredit = averageCurrentCredit;
    }

    public BigDecimal getAverageTargetCredit() {
        return averageTargetCredit;
    }

    public void setAverageTargetCredit(BigDecimal averageTargetCredit) {
        this.averageTargetCredit = averageTargetCredit;
    }

    public BigDecimal getAchievementRate() {
        return achievementRate;
    }

    public void setAchievementRate(BigDecimal achievementRate) {
        this.achievementRate = achievementRate;
    }

    public BigDecimal getTimeProgress() {
        return timeProgress;
    }

    public void setTimeProgress(BigDecimal timeProgress) {
        this.timeProgress = timeProgress;
    }

    public BigDecimal getScheduleTarget() {
        return scheduleTarget;
    }

    public void setScheduleTarget(BigDecimal scheduleTarget) {
        this.scheduleTarget = scheduleTarget;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }

    public Boolean getIsWarning() {
        return isWarning;
    }

    public void setIsWarning(Boolean isWarning) {
        this.isWarning = isWarning;
    }
}
