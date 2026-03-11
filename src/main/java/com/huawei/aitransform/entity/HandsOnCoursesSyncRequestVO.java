package com.huawei.aitransform.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 实战课程个人完课情况同步 - 请求参数
 * 对应接口：POST /external-api/handsOnCoursesSync
 */
public class HandsOnCoursesSyncRequestVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工号（必填）
     */
    private String account;

    /**
     * 课程类型（必填）
     */
    @JsonProperty("task_type")
    private String taskType;

    /**
     * 课程状态（必填）
     */
    @JsonProperty("task_status")
    private String taskStatus;

    /**
     * 课程备注（选填，为空时更新为空）
     */
    @JsonProperty("task_info")
    private String taskInfo;

    public HandsOnCoursesSyncRequestVO() {
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(String taskInfo) {
        this.taskInfo = taskInfo;
    }
}
