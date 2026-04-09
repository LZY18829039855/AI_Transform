package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 个人学分实体类
 */
public class PersonalCredit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 工号
     */
    private String employeeNumber;

    /**
     * 姓名
     */
    private String lastName;

    /**
     * 最小部门编号
     */
    private String lowestDeptNumber;

    /**
     * 最小部门名称
     */
    private String lowestDept;

    /**
     * 公司编码
     */
    private String l0DepartmentCode;

    /**
     * 公司名称
     */
    private String l0DepartmentCnName;

    /**
     * 一层组织编码
     */
    private String firstdeptcode;

    /**
     * 一层组织名称
     */
    private String firstdept;

    /**
     * 二层组织编码
     */
    private String seconddeptcode;

    /**
     * 二层组织名称
     */
    private String seconddept;

    /**
     * 三层组织编码
     */
    private String thirddeptcode;

    /**
     * 三层组织名称
     */
    private String thirddept;

    /**
     * 四层组织编码
     */
    private String fourthdeptcode;

    /**
     * 四层组织名称
     */
    private String fourthdept;

    /**
     * 五层组织编码
     */
    private String fifthdeptcode;

    /**
     * 五层组织名称
     */
    private String fifthdept;

    /**
     * 六层组织编码
     */
    private String sixthdeptcode;

    /**
     * 六层组织名称
     */
    private String sixthdept;

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
    private String jobSubcategory;

    /**
     * 目标学分
     */
    private BigDecimal targetCredit;

    /**
     * 当前学分
     */
    private BigDecimal currentCredit;

    /**
     * 个人学分达成率
     */
    private BigDecimal personalCreditCompletionRate;

    /**
     * 部门最小标杆学分达成率
     */
    private BigDecimal deptBenchmarkCompletionRate;

    /**
     * 学分达成日期
     */
    private Date creditCompletionDate;

    /**
     * 干部岗位AI成熟度等级（来自t_cadre.position_ai_maturity）
     */
    private String cadrePositionAiMaturity;

    /**
     * 专家岗位AI成熟度等级（来自t_expert.position_ai_maturity）
     */
    private String expertPositionAiMaturity;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLowestDeptNumber() {
        return lowestDeptNumber;
    }

    public void setLowestDeptNumber(String lowestDeptNumber) {
        this.lowestDeptNumber = lowestDeptNumber;
    }

    public String getLowestDept() {
        return lowestDept;
    }

    public void setLowestDept(String lowestDept) {
        this.lowestDept = lowestDept;
    }

    public String getL0DepartmentCode() {
        return l0DepartmentCode;
    }

    public void setL0DepartmentCode(String l0DepartmentCode) {
        this.l0DepartmentCode = l0DepartmentCode;
    }

    public String getL0DepartmentCnName() {
        return l0DepartmentCnName;
    }

    public void setL0DepartmentCnName(String l0DepartmentCnName) {
        this.l0DepartmentCnName = l0DepartmentCnName;
    }

    public String getFirstdeptcode() {
        return firstdeptcode;
    }

    public void setFirstdeptcode(String firstdeptcode) {
        this.firstdeptcode = firstdeptcode;
    }

    public String getFirstdept() {
        return firstdept;
    }

    public void setFirstdept(String firstdept) {
        this.firstdept = firstdept;
    }

    public String getSeconddeptcode() {
        return seconddeptcode;
    }

    public void setSeconddeptcode(String seconddeptcode) {
        this.seconddeptcode = seconddeptcode;
    }

    public String getSeconddept() {
        return seconddept;
    }

    public void setSeconddept(String seconddept) {
        this.seconddept = seconddept;
    }

    public String getThirddeptcode() {
        return thirddeptcode;
    }

    public void setThirddeptcode(String thirddeptcode) {
        this.thirddeptcode = thirddeptcode;
    }

    public String getThirddept() {
        return thirddept;
    }

    public void setThirddept(String thirddept) {
        this.thirddept = thirddept;
    }

    public String getFourthdeptcode() {
        return fourthdeptcode;
    }

    public void setFourthdeptcode(String fourthdeptcode) {
        this.fourthdeptcode = fourthdeptcode;
    }

    public String getFourthdept() {
        return fourthdept;
    }

    public void setFourthdept(String fourthdept) {
        this.fourthdept = fourthdept;
    }

    public String getFifthdeptcode() {
        return fifthdeptcode;
    }

    public void setFifthdeptcode(String fifthdeptcode) {
        this.fifthdeptcode = fifthdeptcode;
    }

    public String getFifthdept() {
        return fifthdept;
    }

    public void setFifthdept(String fifthdept) {
        this.fifthdept = fifthdept;
    }

    public String getSixthdeptcode() {
        return sixthdeptcode;
    }

    public void setSixthdeptcode(String sixthdeptcode) {
        this.sixthdeptcode = sixthdeptcode;
    }

    public String getSixthdept() {
        return sixthdept;
    }

    public void setSixthdept(String sixthdept) {
        this.sixthdept = sixthdept;
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

    public String getJobSubcategory() {
        return jobSubcategory;
    }

    public void setJobSubcategory(String jobSubcategory) {
        this.jobSubcategory = jobSubcategory;
    }

    public BigDecimal getTargetCredit() {
        return targetCredit;
    }

    public void setTargetCredit(BigDecimal targetCredit) {
        this.targetCredit = targetCredit;
    }

    public BigDecimal getCurrentCredit() {
        return currentCredit;
    }

    public void setCurrentCredit(BigDecimal currentCredit) {
        this.currentCredit = currentCredit;
    }

    public BigDecimal getPersonalCreditCompletionRate() {
        return personalCreditCompletionRate;
    }

    public void setPersonalCreditCompletionRate(BigDecimal personalCreditCompletionRate) {
        this.personalCreditCompletionRate = personalCreditCompletionRate;
    }

    public BigDecimal getDeptBenchmarkCompletionRate() {
        return deptBenchmarkCompletionRate;
    }

    public void setDeptBenchmarkCompletionRate(BigDecimal deptBenchmarkCompletionRate) {
        this.deptBenchmarkCompletionRate = deptBenchmarkCompletionRate;
    }

    public Date getCreditCompletionDate() {
        return creditCompletionDate;
    }

    public void setCreditCompletionDate(Date creditCompletionDate) {
        this.creditCompletionDate = creditCompletionDate;
    }

    public String getCadrePositionAiMaturity() {
        return cadrePositionAiMaturity;
    }

    public void setCadrePositionAiMaturity(String cadrePositionAiMaturity) {
        this.cadrePositionAiMaturity = cadrePositionAiMaturity;
    }

    public String getExpertPositionAiMaturity() {
        return expertPositionAiMaturity;
    }

    public void setExpertPositionAiMaturity(String expertPositionAiMaturity) {
        this.expertPositionAiMaturity = expertPositionAiMaturity;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}