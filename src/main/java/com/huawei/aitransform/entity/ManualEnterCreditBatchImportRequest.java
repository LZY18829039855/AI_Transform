package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 批量导入学分请求体（一次请求多条，对应独立批量接口）
 */
public class ManualEnterCreditBatchImportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待导入记录（不含 id；modifierNumber 由服务端根据 Cookie 写入，可忽略客户端传入）
     */
    private List<ManualEnterCredit> rows = new ArrayList<>();

    public List<ManualEnterCredit> getRows() {
        return rows;
    }

    public void setRows(List<ManualEnterCredit> rows) {
        this.rows = rows == null ? new ArrayList<ManualEnterCredit>() : rows;
    }
}
