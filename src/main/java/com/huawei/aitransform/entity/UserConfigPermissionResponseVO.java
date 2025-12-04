package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 用户权限查询响应VO
 */
public class UserConfigPermissionResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 管理员工号列表
     */
    private List<String> adminAccounts;

    /**
     * 非管理员工号列表
     */
    private List<String> nonAdminAccounts;

    public UserConfigPermissionResponseVO() {
    }

    public UserConfigPermissionResponseVO(List<String> adminAccounts, List<String> nonAdminAccounts) {
        this.adminAccounts = adminAccounts;
        this.nonAdminAccounts = nonAdminAccounts;
    }

    public List<String> getAdminAccounts() {
        return adminAccounts;
    }

    public void setAdminAccounts(List<String> adminAccounts) {
        this.adminAccounts = adminAccounts;
    }

    public List<String> getNonAdminAccounts() {
        return nonAdminAccounts;
    }

    public void setNonAdminAccounts(List<String> nonAdminAccounts) {
        this.nonAdminAccounts = nonAdminAccounts;
    }
}


