package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 员工详细信息VO
 */
public class EmployeeDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 姓名
     */
    private String name;

    /**
     * 工号
     */
    private String employeeNumber;

    /**
     * 职位类
     */
    private String competenceCategory;

    /**
     * 职位子类
     */
    private String competenceSubcategory;

    /**
     * 一级部门名称
     */
    private String departname1;

    /**
     * 二级部门名称
     */
    private String departname2;

    /**
     * 三级部门名称
     */
    private String departname3;

    /**
     * 四级部门名称
     */
    private String departname4;

    /**
     * 五级部门名称
     */
    private String departname5;

    /**
     * 六级部门名称
     */
    private String departname6;

    /**
     * 七级部门名称
     */
    private String departname7;

    /**
     * AI类证书名称（认证数据时使用）
     */
    private String certTitle;

    /**
     * 证书开始时间（认证数据时使用）
     */
    private Date certStartTime;

    /**
     * 是否通过科目二（认证数据时使用，0-未通过，1-通过）
     */
    private Integer isPassedSubject2;

    public EmployeeDetailVO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getCompetenceCategory() {
        return competenceCategory;
    }

    public void setCompetenceCategory(String competenceCategory) {
        this.competenceCategory = competenceCategory;
    }

    public String getCompetenceSubcategory() {
        return competenceSubcategory;
    }

    public void setCompetenceSubcategory(String competenceSubcategory) {
        this.competenceSubcategory = competenceSubcategory;
    }

    public String getDepartname1() {
        return departname1;
    }

    public void setDepartname1(String departname1) {
        this.departname1 = departname1;
    }

    public String getDepartname2() {
        return departname2;
    }

    public void setDepartname2(String departname2) {
        this.departname2 = departname2;
    }

    public String getDepartname3() {
        return departname3;
    }

    public void setDepartname3(String departname3) {
        this.departname3 = departname3;
    }

    public String getDepartname4() {
        return departname4;
    }

    public void setDepartname4(String departname4) {
        this.departname4 = departname4;
    }

    public String getDepartname5() {
        return departname5;
    }

    public void setDepartname5(String departname5) {
        this.departname5 = departname5;
    }

    public String getDepartname6() {
        return departname6;
    }

    public void setDepartname6(String departname6) {
        this.departname6 = departname6;
    }

    public String getDepartname7() {
        return departname7;
    }

    public void setDepartname7(String departname7) {
        this.departname7 = departname7;
    }

    public String getCertTitle() {
        return certTitle;
    }

    public void setCertTitle(String certTitle) {
        this.certTitle = certTitle;
    }

    public Date getCertStartTime() {
        return certStartTime;
    }

    public void setCertStartTime(Date certStartTime) {
        this.certStartTime = certStartTime;
    }

    public Integer getIsPassedSubject2() {
        return isPassedSubject2;
    }

    public void setIsPassedSubject2(Integer isPassedSubject2) {
        this.isPassedSubject2 = isPassedSubject2;
    }
}

