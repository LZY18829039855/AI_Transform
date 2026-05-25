package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.DepartmentEmployeeCourseCompletionDetailVO;

/**
 * 部门全员目标课程完课明细（矩阵导出）
 */
public interface DepartmentEmployeeCourseCompletionDetailService {

    /**
     * @param deptId     部门编码
     * @param personType 0 全员；1 干部；2 专家
     * @param aiMaturity 岗位 AI 成熟度 L1/L2/L3（可选）
     */
    DepartmentEmployeeCourseCompletionDetailVO getDepartmentEmployeeCourseCompletionDetail(
            String deptId, Integer personType, String aiMaturity);
}
