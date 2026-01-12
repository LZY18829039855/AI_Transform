package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 用户工号信息响应VO
 */
public class UserAccountResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 带首字母的工号信息（emp_num）
     */
    private String empNum;

    /**
     * 去除首字母的工号信息（w3_account）
     */
    private String w3Account;

    public UserAccountResponseVO() {
    }

    public UserAccountResponseVO(String empNum, String w3Account) {
        this.empNum = empNum;
        this.w3Account = w3Account;
    }

    public String getEmpNum() {
        return empNum;
    }

    public void setEmpNum(String empNum) {
        this.empNum = empNum;
    }

    public String getW3Account() {
        return w3Account;
    }

    public void setW3Account(String w3Account) {
        this.w3Account = w3Account;
    }
}

