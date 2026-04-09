package com.huawei.aitransform.service;

import com.huawei.aitransform.common.PageResult;
import com.huawei.aitransform.entity.ManualEnterCredit;
import com.huawei.aitransform.entity.ManualEnterCreditBatchImportResult;

import java.util.List;

/**
 * 手动录入学分服务
 */
public interface ManualEnterCreditService {

    /**
     * 分页查询
     *
     * @param employeeNumber 工号（模糊，可空）
     * @param employeeName   姓名（模糊，可空）
     * @param pageNum        页码，从 1 开始
     * @param pageSize       每页条数
     */
    PageResult<ManualEnterCredit> page(String employeeNumber, String employeeName, int pageNum, int pageSize);

    ManualEnterCredit getById(Integer id);

    /**
     * 新增；modifierNumber 由调用方从 Cookie 解析后传入，写入库表 Modifier__number
     */
    ManualEnterCredit create(ManualEnterCredit record, String modifierNumber);

    /**
     * 全量更新；modifierNumber 由调用方从 Cookie 解析后传入，覆盖库表 Modifier__number
     */
    ManualEnterCredit update(Integer id, ManualEnterCredit record, String modifierNumber);

    /**
     * 批量导入：一次请求插入多行（独立接口，非多次单次新增）
     */
    ManualEnterCreditBatchImportResult batchImport(List<ManualEnterCredit> rows, String modifierNumber);

    boolean delete(Integer id);
}
