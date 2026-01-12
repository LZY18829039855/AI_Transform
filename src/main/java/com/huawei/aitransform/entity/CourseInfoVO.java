package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 课程信息VO
 */
public class CourseInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程编码
     */
    private String courseNumber;

    /**
     * 是否已完成
     */
    private Boolean isCompleted;

    public CourseInfoVO() {
    }

    public CourseInfoVO(String courseName, String courseNumber, Boolean isCompleted) {
        this.courseName = courseName;
        this.courseNumber = courseNumber;
        this.isCompleted = isCompleted;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
}

