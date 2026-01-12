package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 按级别分类的课程信息VO（用于Mapper查询结果）
 */
public class CourseInfoByLevelVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程级别（训战分类）
     */
    private String courseLevel;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程编码
     */
    private String courseNumber;

    /**
     * 课程主分类
     */
    private String bigType;

    /**
     * 课程链接
     */
    private String courseLink;

    public CourseInfoByLevelVO() {
    }

    public String getCourseLevel() {
        return courseLevel;
    }

    public void setCourseLevel(String courseLevel) {
        this.courseLevel = courseLevel;
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

    public String getBigType() {
        return bigType;
    }

    public void setBigType(String bigType) {
        this.bigType = bigType;
    }

    public String getCourseLink() {
        return courseLink;
    }

    public void setCourseLink(String courseLink) {
        this.courseLink = courseLink;
    }
}

