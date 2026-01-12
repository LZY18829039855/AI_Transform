package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 课程分类统计VO
 */
public class CourseCategoryStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 训战分类（课程级别）
     */
    private String courseLevel;

    /**
     * 课程主分类
     */
    private String bigType;

    /**
     * 课程总数
     */
    private Integer totalCourses;

    /**
     * 目标课程数（等于课程总数）
     */
    private Integer targetCourses;

    /**
     * 实际完课数
     */
    private Integer completedCourses;

    /**
     * 完课占比（百分比，保留2位小数）
     */
    private Double completionRate;

    /**
     * 该分类下的所有目标课程列表（包含已完成和未完成的课程）
     */
    private List<CourseInfoVO> courseList;

    public CourseCategoryStatisticsVO() {
    }

    public String getCourseLevel() {
        return courseLevel;
    }

    public void setCourseLevel(String courseLevel) {
        this.courseLevel = courseLevel;
    }

    public String getBigType() {
        return bigType;
    }

    public void setBigType(String bigType) {
        this.bigType = bigType;
    }

    public Integer getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(Integer totalCourses) {
        this.totalCourses = totalCourses;
    }

    public Integer getTargetCourses() {
        return targetCourses;
    }

    public void setTargetCourses(Integer targetCourses) {
        this.targetCourses = targetCourses;
    }

    public Integer getCompletedCourses() {
        return completedCourses;
    }

    public void setCompletedCourses(Integer completedCourses) {
        this.completedCourses = completedCourses;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public List<CourseInfoVO> getCourseList() {
        return courseList;
    }

    public void setCourseList(List<CourseInfoVO> courseList) {
        this.courseList = courseList;
    }
}

