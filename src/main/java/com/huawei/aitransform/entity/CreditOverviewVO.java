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
     * 时间进度
     */
    private BigDecimal timeProgress;

    /**
     * 是否预警
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

    public Boolean getIsWarning() {
        return isWarning;
    }

    public void setIsWarning(Boolean isWarning) {
        this.isWarning = isWarning;
    }
}
