package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 当前用户权限状态：白名单成员、管理员与多元化学分写权限标识
 */
public class UserPermissionStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否在 user_config 白名单内（未删除）
     */
    private boolean member;

    /**
     * 是否为管理员（仅 member 为 true 时可能为 true）
     */
    private boolean asAdmin;

    /**
     * 是否可新增、编辑、删除、导入多元化学分
     */
    private boolean canEditCredit;

    public UserPermissionStatusVO() {
    }

    public UserPermissionStatusVO(boolean member, boolean asAdmin) {
        this(member, asAdmin, false);
    }

    public UserPermissionStatusVO(boolean member, boolean asAdmin, boolean canEditCredit) {
        this.member = member;
        this.asAdmin = asAdmin;
        this.canEditCredit = canEditCredit;
    }

    public boolean isMember() {
        return member;
    }

    public void setMember(boolean member) {
        this.member = member;
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
