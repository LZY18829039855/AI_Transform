package com.huawei.aitransform.service;

import java.util.Map;

/**
 * 全体员工训战信息同步服务
 */
public interface EmployeeTrainingInfoSyncService {

    /**
     * 同步员工训战信息到 t_employee_training_info
     * @param periodId 期号（yyyyMMdd）
     * @return 同步结果统计
     */
    Map<String, Object> syncEmployeeTrainingInfo(String periodId);
}
