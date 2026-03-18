package com.huawei.aitransform.entity;

/**
 * 实战课程信息表 ai_practical_course_info 实体
 * 用于按 task_type 查询课程主键 ID
 */
public class PracticalCourseInfo {

    /**
     * 主键 ID
     */
    private Integer id;

    /**
     * 课程类型（与 hands_on_courses.task_type 对应）
     */
    private String taskType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
}
