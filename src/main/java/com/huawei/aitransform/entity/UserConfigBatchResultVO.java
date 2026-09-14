package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限管理：批量新增/覆盖结果
 */
public class UserConfigBatchResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int createdCount;
    private int updatedCount;
    private int failedCount;
    private List<Item> items = new ArrayList<>();

    public int getCreatedCount() {
        return createdCount;
    }

    public void setCreatedCount(int createdCount) {
        this.createdCount = createdCount;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void setUpdatedCount(int updatedCount) {
        this.updatedCount = updatedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items == null ? new ArrayList<Item>() : items;
    }

    public static class Item implements Serializable {
        private static final long serialVersionUID = 1L;

        private String account;
        /** created / updated / failed */
        private String status;
        private String message;

        public Item() {
        }

        public Item(String account, String status, String message) {
            this.account = account;
            this.status = status;
            this.message = message;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
