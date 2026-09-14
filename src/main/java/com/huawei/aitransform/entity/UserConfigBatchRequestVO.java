package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 权限管理：批量新增/覆盖请求（同一套权限位）
 */
public class UserConfigBatchRequestVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工号列表 */
    private List<String> accounts;

    /** 是否管理员 */
    private boolean asAdmin;

    /** 是否超级用户（可维护多元化学分、权限配置等）；仅 asAdmin=true 时生效 */
    private boolean canEditCredit;

    public List<String> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<String> accounts) {
        this.accounts = accounts;
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
