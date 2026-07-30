package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 用户权限配置VO
 */
public class UserConfigVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 工号
     */
    private String account;

    /**
     * 是否为管理员
     */
    private String isAdmin;

    /**
     * 是否可新增、编辑、删除、导入多元化学分
     */
    private String canEditCredit;

    /**
     * 是否删除
     */
    private String isDeleted;

    public UserConfigVO() {
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

    public String getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(String isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getCanEditCredit() {
        return canEditCredit;
    }

    public void setCanEditCredit(String canEditCredit) {
        this.canEditCredit = canEditCredit;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }
}








