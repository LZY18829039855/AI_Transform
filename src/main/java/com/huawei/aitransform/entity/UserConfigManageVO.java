package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 权限管理页用户配置（列表/新增/编辑）
 */
public class UserConfigManageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 工号（入库时去掉首字母）
     */
    private String account;

    /**
     * 员工姓名（来自员工表，可空）
     */
    private String employeeName;

    /**
     * 是否管理员
     */
    private boolean asAdmin;

    /**
     * 是否超级用户（可维护多元化学分、权限配置等）
     */
    private boolean canEditCredit;

    public UserConfigManageVO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public boolean isAsAdmin() {
        return asAdmin;
    }

    public void setAsAdmin(boolean asAdmin) {
        this.asAdmin = asAdmin;
    }

    public boolean isCanEditCredit() {
        return canEditCredit;
    }

    public void setCanEditCredit(boolean canEditCredit) {
        this.canEditCredit = canEditCredit;
    }
}
