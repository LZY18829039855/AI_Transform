package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 干部部门信息刷新VO
 */
public class CadreDepartmentRefreshVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工工号
     */
    private String account;

    /**
     * 最小部门ID
     */
    private String miniDepartnameId;

    /**
     * 二级部门编码
     */
    private String l2DepartmentCode;

    /**
     * 三级部门编码
     */
    private String l3DepartmentCode;

    /**
     * 四级部门编码
     */
    private String l4DepartmentCode;

    /**
     * 五级部门编码
     */
    private String l5DepartmentCode;

    public CadreDepartmentRefreshVO() {
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getMiniDepartnameId() {
        return miniDepartnameId;
    }

    public void setMiniDepartnameId(String miniDepartnameId) {
        this.miniDepartnameId = miniDepartnameId;
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
}

