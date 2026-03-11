package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 实战课程个人完课情况实体类
 * 对应表：hands_on_courses
 */
public class HandsOnCourse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工号
     */
    private String account;

    /**
     * 课程类型
     */
    private String taskType;

    /**
     * 课程状态
     */
    private String taskStatus;

    /**
     * 课程备注
     */
    private String taskInfo;

    /**
     * 更新时间
     */
    private Date updateTime;

    public HandsOnCourse() {
    }

    public HandsOnCourse(String account, String taskType, String taskStatus, String taskInfo, Date updateTime) {
        this.account = account;
        this.taskType = taskType;
        this.taskStatus = taskStatus;
        this.taskInfo = taskInfo;
        this.updateTime = updateTime;
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
