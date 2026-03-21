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
     * @param personType 人员类型：0 全员；1 干部；2 专家
     * @param aiMaturity 岗位 AI 成熟度（可选）：L1、L2、L3；仅在与干部/专家类型联用时生效，对应过滤 cadre_position_ai_maturity / expert_position_ai_maturity
     * @return 该部门下每名员工的训战总览列表；部门不存在或无人员时返回空列表
     */
    List<DepartmentEmployeeTrainingOverviewVO> getDepartmentEmployeeTrainingOverview(String deptId, Integer personType, String aiMaturity);
}
