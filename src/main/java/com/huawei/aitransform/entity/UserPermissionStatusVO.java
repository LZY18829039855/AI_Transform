package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 当前用户权限状态：白名单成员与管理员标识
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

    public UserPermissionStatusVO() {
    }

    public UserPermissionStatusVO(boolean member, boolean asAdmin) {
        this.member = member;
        this.asAdmin = asAdmin;
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
}
