package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.DepartmentEmployeeTrainingOverviewVO;

import java.util.List;

/**
 * 部门全员训战总览（下钻）服务
 */
public interface DepartmentEmployeeTrainingOverviewService {

    /**
     * 根据部门ID与人员类型查询该部门下全员训战总览明细
     * @param deptId    部门ID（部门编码）
     * @param personType 人员类型，当前仅处理 0
     * @return 该部门下每名员工的训战总览列表；部门不存在或无人员时返回空列表
     */
    List<DepartmentEmployeeTrainingOverviewVO> getDepartmentEmployeeTrainingOverview(String deptId, Integer personType);
}
