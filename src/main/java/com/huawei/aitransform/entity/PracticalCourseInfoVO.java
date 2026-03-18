package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 实战课程信息 VO（对应 ai_practical_course_info）
 */
public class PracticalCourseInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    private Integer id;

    /**
     * 实战课程类型（与 hands_on_courses.task_type 对应）
     */
    private String taskType;

    /**
     * 实战课程名称
     */
    private String taskName;

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

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
