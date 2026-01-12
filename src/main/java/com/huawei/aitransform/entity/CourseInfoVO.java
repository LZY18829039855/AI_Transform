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

    /**
     * 课程主分类
     */
    private String bigType;

    /**
     * 是否目标课程
     */
    private Boolean isTargetCourse;

    /**
     * 课程链接
     */
    private String courseLink;

    public CourseInfoVO() {
    }

    public CourseInfoVO(String courseName, String courseNumber, Boolean isCompleted) {
        this.courseName = courseName;
        this.courseNumber = courseNumber;
        this.isCompleted = isCompleted;
    }

    public CourseInfoVO(String courseName, String courseNumber, Boolean isCompleted, String bigType) {
        this.courseName = courseName;
        this.courseNumber = courseNumber;
        this.isCompleted = isCompleted;
        this.bigType = bigType;
    }

    public CourseInfoVO(String courseName, String courseNumber, Boolean isCompleted, String bigType, Boolean isTargetCourse, String courseLink) {
        this.courseName = courseName;
        this.courseNumber = courseNumber;
        this.isCompleted = isCompleted;
        this.bigType = bigType;
        this.isTargetCourse = isTargetCourse;
        this.courseLink = courseLink;
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

    public String getBigType() {
        return bigType;
    }

    public void setBigType(String bigType) {
        this.bigType = bigType;
    }

    public Boolean getIsTargetCourse() {
        return isTargetCourse;
    }

    public void setIsTargetCourse(Boolean isTargetCourse) {
        this.isTargetCourse = isTargetCourse;
    }

    public String getCourseLink() {
        return courseLink;
    }

    public void setCourseLink(String courseLink) {
        this.courseLink = courseLink;
    }
}

