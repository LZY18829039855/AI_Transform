package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * AI School学分数据明细VO
 * 用于展示员工学分详情的下钻数据
 */
public class SchoolCreditDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 工号
     */
    private String employeeId;

    /**
     * 职位族
     */
    private String jobFamily;

    /**
     * 职位类
     */
    private String jobCategory;

    /**
     * 职位子类
     */
    private String jobSubCategory;

    /**
     * 一级部门
     */
    private String departmentLevel1;

    /**
     * 二级部门
     */
    private String departmentLevel2;

    /**
     * 三级部门
     */
    private String departmentLevel3;

    /**
     * 四级部门
     */
    private String departmentLevel4;

    /**
     * 五级部门
     */
    private String departmentLevel5;

    /**
     * 六级部门
     */
    private String departmentLevel6;

    /**
     * 最小部门
     */
    private String minDepartment;

    /**
     * 是否干部（0-否，1-是）
     */
    private Integer isCadre;

    /**
     * 干部类型
     */
    private String cadreType;

    /**
     * 是否专家（0-否，1-是）
     */
    private Integer isExpert;

    /**
     * 是否基层主管（0-否，1-是）
     */
    private Integer isFrontlineManager;

    /**
     * 组织AI成熟度
     */
    private String organizationMaturity;

    /**
     * 岗位AI成熟度
     */
    private String positionMaturity;

    /**
     * 当前学分
     */
    private BigDecimal currentCredits;

    /**
     * 学分达成率（百分比，如 85.5 表示 85.5%）
     */
    private BigDecimal completionRate;

    /**
     * 所在最小部门标杆学分达成率
     */
    private BigDecimal benchmarkRate;

    /**
     * 学分达成日期
     */
    private String completionDate;

    /**
     * 时间进度学分目标
     */
    private BigDecimal scheduleTarget;

    /**
     * 学分状态预警（正常、轻度预警、滞后预警）
     */
    private String status;

    /**
     * 状态类型（success、warning、danger）
     */
    private String statusType;

    public SchoolCreditDetailVO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getJobFamily() {
        return jobFamily;
    }

    public void setJobFamily(String jobFamily) {
        this.jobFamily = jobFamily;
    }

    public String getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
    }

    public String getJobSubCategory() {
        return jobSubCategory;
    }

    public void setJobSubCategory(String jobSubCategory) {
        this.jobSubCategory = jobSubCategory;
    }

    public String getDepartmentLevel1() {
        return departmentLevel1;
    }

    public void setDepartmentLevel1(String departmentLevel1) {
        this.departmentLevel1 = departmentLevel1;
    }

    public String getDepartmentLevel2() {
        return departmentLevel2;
    }

    public void setDepartmentLevel2(String departmentLevel2) {
        this.departmentLevel2 = departmentLevel2;
    }

    public String getDepartmentLevel3() {
        return departmentLevel3;
    }

    public void setDepartmentLevel3(String departmentLevel3) {
        this.departmentLevel3 = departmentLevel3;
    }

    public String getDepartmentLevel4() {
        return departmentLevel4;
    }

    public void setDepartmentLevel4(String departmentLevel4) {
        this.departmentLevel4 = departmentLevel4;
    }

    public String getDepartmentLevel5() {
        return departmentLevel5;
    }

    public void setDepartmentLevel5(String departmentLevel5) {
        this.departmentLevel5 = departmentLevel5;
    }

    public String getDepartmentLevel6() {
        return departmentLevel6;
    }

    public void setDepartmentLevel6(String departmentLevel6) {
        this.departmentLevel6 = departmentLevel6;
    }

    public String getMinDepartment() {
        return minDepartment;
    }

    public void setMinDepartment(String minDepartment) {
        this.minDepartment = minDepartment;
    }

    public Integer getIsCadre() {
        return isCadre;
    }

    public void setIsCadre(Integer isCadre) {
        this.isCadre = isCadre;
    }

    public String getCadreType() {
        return cadreType;
    }

    public void setCadreType(String cadreType) {
        this.cadreType = cadreType;
    }

    public Integer getIsExpert() {
        return isExpert;
    }

    public void setIsExpert(Integer isExpert) {
        this.isExpert = isExpert;
    }

    public Integer getIsFrontlineManager() {
        return isFrontlineManager;
    }

    public void setIsFrontlineManager(Integer isFrontlineManager) {
        this.isFrontlineManager = isFrontlineManager;
    }

    public String getOrganizationMaturity() {
        return organizationMaturity;
    }

    public void setOrganizationMaturity(String organizationMaturity) {
        this.organizationMaturity = organizationMaturity;
    }

    public String getPositionMaturity() {
        return positionMaturity;
    }

    public void setPositionMaturity(String positionMaturity) {
        this.positionMaturity = positionMaturity;
    }

    public BigDecimal getCurrentCredits() {
        return currentCredits;
    }

    public void setCurrentCredits(BigDecimal currentCredits) {
        this.currentCredits = currentCredits;
    }

    public BigDecimal getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(BigDecimal completionRate) {
        this.completionRate = completionRate;
    }

    public BigDecimal getBenchmarkRate() {
        return benchmarkRate;
    }

    public void setBenchmarkRate(BigDecimal benchmarkRate) {
        this.benchmarkRate = benchmarkRate;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
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
}
