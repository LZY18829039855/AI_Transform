package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.PositionAiMaturityCourseCompletionRateVO;

import java.util.List;

/**
 * 专家/干部训战统计（按岗位 AI 成熟度）
 */
public interface PositionAiMaturityTrainingStatsService {

    /**
     * 按部门与人员类型（仅干部/专家）查询各岗位 AI 成熟度下的训战汇总
     *
     * @param deptId     部门编码
     * @param personType 1 干部；2 专家
     * @return 按成熟度分组列表，排序 L1 → L2 → L3 → 其它
     */
    List<PositionAiMaturityCourseCompletionRateVO> listByDeptAndPersonType(String deptId, Integer personType);
}
