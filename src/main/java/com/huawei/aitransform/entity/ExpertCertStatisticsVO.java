package com.huawei.aitransform.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 专家认证统计结果VO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpertCertStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * AI成熟度（L1/L2/L3）
     */
    private String aiMaturity;

    /**
     * 职位类
     */
    private String jobCategory;

    /**
     * 基线人数（总人数）
     */
    private Integer baselineCount;

    /**
     * 已完成AI认证的人数
     */
    private Integer certCount;

    /**
     * AI认证人数占比（百分比）
     */
    private BigDecimal certRate;

    /**
     * 员工工号（用于查询认证状态，不返回给前端）
     */
    private String employeeNumber;

    /**
     * 是否有证书（1-有证书，0-无证书）
     */
    private Integer hasCert;

    /**
     * 职位类统计列表（树形结构中的子节点）
     */
    private List<ExpertCertStatisticsVO> jobCategoryStatistics;

    public ExpertCertStatisticsVO() {
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

    public Integer getBaselineCount() {
        return baselineCount;
    }

    public void setBaselineCount(Integer baselineCount) {
        this.baselineCount = baselineCount;
    }

    public Integer getCertCount() {
        return certCount;
    }

    public void setCertCount(Integer certCount) {
        this.certCount = certCount;
    }

    public BigDecimal getCertRate() {
        return certRate;
    }

    public void setCertRate(BigDecimal certRate) {
        this.certRate = certRate;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public Integer getHasCert() {
        return hasCert;
    }

    public void setHasCert(Integer hasCert) {
        this.hasCert = hasCert;
    }

    public List<ExpertCertStatisticsVO> getJobCategoryStatistics() {
        return jobCategoryStatistics;
    }

    public void setJobCategoryStatistics(List<ExpertCertStatisticsVO> jobCategoryStatistics) {
        this.jobCategoryStatistics = jobCategoryStatistics;
    }
}

