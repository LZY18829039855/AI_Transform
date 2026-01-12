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
}

