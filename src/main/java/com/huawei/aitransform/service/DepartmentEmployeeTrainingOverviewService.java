package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.DepartmentEmployeeTrainingOverviewResponseVO;
import com.huawei.aitransform.entity.DepartmentEmployeeTrainingOverviewVO;

import java.util.List;

/**
 * 部门全员训战总览（下钻）服务
 */
public interface DepartmentEmployeeTrainingOverviewService {

    /**
     * 根据部门ID与人员类型查询该部门下全员训战总览明细（不分页，兼容旧调用）
     */
    List<DepartmentEmployeeTrainingOverviewVO> getDepartmentEmployeeTrainingOverview(
            String deptId, Integer personType, String aiMaturity);

    /**
     * 部门全员训战总览下钻（分页，对齐学分明细）
     *
     * @param deptId          部门ID
     * @param personType      0 全员 / 1 干部 / 2 专家
     * @param aiMaturity      可选 L1/L2/L3
     * @param name            姓名模糊（可选）
     * @param employeeNumber  工号模糊（可选）
     * @param pageNum         页码，从 1 开始
     * @param pageSize        每页条数
     */
    DepartmentEmployeeTrainingOverviewResponseVO getDepartmentEmployeeTrainingOverviewPage(
            String deptId,
            Integer personType,
            String aiMaturity,
            String name,
            String employeeNumber,
            Integer pageNum,
            Integer pageSize);
}
