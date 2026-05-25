package com.huawei.aitransform.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 部门完课矩阵导出：动态课程列定义
 */
@Data
public class CourseCompletionColumnVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 列唯一键，如 c_12 */
    private String key;
    /** 表头，如 基础-人工智能导论 */
    private String header;
    /** 课程级别：基础 / 进阶 / 实战 */
    private String courseLevel;
    /** 课程主键 ai_course_planning_info.id */
    private Integer courseId;
}
