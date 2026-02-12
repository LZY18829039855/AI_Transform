package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.DepartmentCourseCompletionRateVO;

import java.util.List;

/**
 * 部门课程完成率查询服务
 */
public interface DepartmentCourseCompletionRateService {

    /**
     * 根据父部门ID与人员类型查询下一层级各部门的课程完成率统计
     * @param deptId    父部门ID（0 或二级部门时返回所有四级部门；三级返回四级子部门；四/五/六级返回下一层级子部门）
     * @param personType 人员类型，当前仅处理 0
     * @return 各部门统计列表
     */
    List<DepartmentCourseCompletionRateVO> getDepartmentCourseCompletionRate(String deptId, Integer personType);
}
