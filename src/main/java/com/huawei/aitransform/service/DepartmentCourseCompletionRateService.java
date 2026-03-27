package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.DepartmentCourseCompletionRateVO;

import java.util.List;

/**
 * 部门课程完成率查询服务
 */
public interface DepartmentCourseCompletionRateService {

    /**
     * 根据父部门ID与人员类型查询下一层级各部门的课程完成率统计；无子部门时返回本部门一条
     * @param deptId    父部门ID（0 或二级部门时返回所有四级部门；三级返回四级子部门；四/五/六级返回下一层级子部门；无子部门时为本部门）
     * @param personType 人员类型：0 全员；1 干部（cadre_position_ai_maturity 非空）；2 专家（expert_position_ai_maturity 非空）
     * @return 各部门统计列表
     */
    List<DepartmentCourseCompletionRateVO> getDepartmentCourseCompletionRate(String deptId, Integer personType);
}
