package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 批量导入结果
 */
public class ManualEnterCreditBatchImportResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private int insertedCount;

    public ManualEnterCreditBatchImportResult() {
    }

    public ManualEnterCreditBatchImportResult(int insertedCount) {
        this.insertedCount = insertedCount;
    }

    public int getInsertedCount() {
        return insertedCount;
    }

    public void setInsertedCount(int insertedCount) {
        this.insertedCount = insertedCount;
    }
}
