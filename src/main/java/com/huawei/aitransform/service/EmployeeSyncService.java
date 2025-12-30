package com.huawei.aitransform.service;

import java.util.Map;

public interface EmployeeSyncService {
    /**
     * 同步员工数据
     * @param periodId 期号
     * @return 同步结果统计
     */
    Map<String, Object> syncEmployeeData(String periodId);
}
