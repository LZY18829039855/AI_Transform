package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 权限管理：部门成员候选项
 */
public class UserConfigDeptMemberVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工号 */
    private String account;

    /** 姓名 */
    private String employeeName;

    /** 是否已有有效权限配置 */
    private boolean alreadyConfigured;

    public UserConfigDeptMemberVO() {
    }

    public UserConfigDeptMemberVO(String account, String employeeName, boolean alreadyConfigured) {
        this.account = account;
        this.employeeName = employeeName;
        this.alreadyConfigured = alreadyConfigured;
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

    public boolean isAlreadyConfigured() {
        return alreadyConfigured;
    }

    public void setAlreadyConfigured(boolean alreadyConfigured) {
        this.alreadyConfigured = alreadyConfigured;
    }
}
