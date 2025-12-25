package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 基层主管数据实体�?
 * 对应表：t_entry_level_manager
 */
public class EntryLevelManager implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long employeeAppointmentId;

    /**
     * 员工工号
     */
    private String employeeNumber;

    /**
     * 最小部门编�?
     */
    private String organizationCode;

    /**
     * 最小部门中�?
     */
    private String organizationNameCn;

    /**
     * 职位编码
     */
    private String jobCode;

    /**
     * 职位名称中文
     */
    private String jobNameCn;

    /**
     * 任命开始时�?
     */
    private Date startDate;

    /**
     * 任命结束时间
     */
    private Date endDate;

    /**
     * 状�?
     */
    private String status;

    /**
     * 一层组织编�?
     */
    private String l1DepartmentCode;

    /**
     * 二层组织编码
     */
    private String l2DepartmentCode;

    /**
     * 三层组织编码
     */
    private String l3DepartmentCode;

    /**
     * 四层组织编码
     */
    private String l4DepartmentCode;

    /**
     * 五层组织编码
     */
    private String l5DepartmentCode;

    /**
     * 六层组织编码
     */
    private String l6DepartmentCode;

    /**
     * 七层组织编码
     */
    private String l7DepartmentCode;

    /**
     * 1层组织中文名�?
     */
    private String l1DepartmentCnName;

    /**
     * 2层组织中文名�?
     */
    private String l2DepartmentCnName;

    /**
     * 3层组织中文名�?
     */
    private String l3DepartmentCnName;

    /**
     * 4层组织中文名�?
     */
    private String l4DepartmentCnName;

    /**
     * 5层组织中文名�?
     */
    private String l5DepartmentCnName;

    /**
     * 6层组织中文名�?
     */
    private String l6DepartmentCnName;

    /**
     * 7层组织中文名�?
     */
    private String l7DepartmentCnName;

    /**
     * 职位名称中文（数据库字段：position_name_cn�?
     */
    private String positionNameCn;

    public EntryLevelManager() {
    }

    public Long getEmployeeAppointmentId() {
        return employeeAppointmentId;
    }

    public void setEmployeeAppointmentId(Long employeeAppointmentId) {
        this.employeeAppointmentId = employeeAppointmentId;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String getOrganizationNameCn() {
        return organizationNameCn;
    }

    public void setOrganizationNameCn(String organizationNameCn) {
        this.organizationNameCn = organizationNameCn;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public String getJobNameCn() {
        return jobNameCn;
    }

    public void setJobNameCn(String jobNameCn) {
        this.jobNameCn = jobNameCn;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getL1DepartmentCode() {
        return l1DepartmentCode;
    }

    public void setL1DepartmentCode(String l1DepartmentCode) {
        this.l1DepartmentCode = l1DepartmentCode;
    }

    public String getL2DepartmentCode() {
        return l2DepartmentCode;
    }

    public void setL2DepartmentCode(String l2DepartmentCode) {
        this.l2DepartmentCode = l2DepartmentCode;
    }

    public String getL3DepartmentCode() {
        return l3DepartmentCode;
    }

    public void setL3DepartmentCode(String l3DepartmentCode) {
        this.l3DepartmentCode = l3DepartmentCode;
    }

    public String getL4DepartmentCode() {
        return l4DepartmentCode;
    }

    public void setL4DepartmentCode(String l4DepartmentCode) {
        this.l4DepartmentCode = l4DepartmentCode;
    }

    public String getL5DepartmentCode() {
        return l5DepartmentCode;
    }

    public void setL5DepartmentCode(String l5DepartmentCode) {
        this.l5DepartmentCode = l5DepartmentCode;
    }

    public String getL6DepartmentCode() {
        return l6DepartmentCode;
    }

    public void setL6DepartmentCode(String l6DepartmentCode) {
        this.l6DepartmentCode = l6DepartmentCode;
    }

    public String getL7DepartmentCode() {
        return l7DepartmentCode;
    }

    public void setL7DepartmentCode(String l7DepartmentCode) {
        this.l7DepartmentCode = l7DepartmentCode;
    }

    public String getL1DepartmentCnName() {
        return l1DepartmentCnName;
    }

    public void setL1DepartmentCnName(String l1DepartmentCnName) {
        this.l1DepartmentCnName = l1DepartmentCnName;
    }

    public String getL2DepartmentCnName() {
        return l2DepartmentCnName;
    }

    public void setL2DepartmentCnName(String l2DepartmentCnName) {
        this.l2DepartmentCnName = l2DepartmentCnName;
    }

    public String getL3DepartmentCnName() {
        return l3DepartmentCnName;
    }

    public void setL3DepartmentCnName(String l3DepartmentCnName) {
        this.l3DepartmentCnName = l3DepartmentCnName;
    }

    public String getL4DepartmentCnName() {
        return l4DepartmentCnName;
    }

    public void setL4DepartmentCnName(String l4DepartmentCnName) {
        this.l4DepartmentCnName = l4DepartmentCnName;
    }

    public String getL5DepartmentCnName() {
        return l5DepartmentCnName;
    }

    public void setL5DepartmentCnName(String l5DepartmentCnName) {
        this.l5DepartmentCnName = l5DepartmentCnName;
    }

    public String getL6DepartmentCnName() {
        return l6DepartmentCnName;
    }

    public void setL6DepartmentCnName(String l6DepartmentCnName) {
        this.l6DepartmentCnName = l6DepartmentCnName;
    }

    public String getL7DepartmentCnName() {
        return l7DepartmentCnName;
    }

    public void setL7DepartmentCnName(String l7DepartmentCnName) {
        this.l7DepartmentCnName = l7DepartmentCnName;
    }

    public String getpositionNameCn() {
        return positionNameCn;
    }

    public void setpositionNameCn(String positionNameCn) {
        this.positionNameCn = positionNameCn;
    }
}


