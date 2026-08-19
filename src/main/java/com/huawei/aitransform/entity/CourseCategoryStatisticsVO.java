package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.math.BigDecimal;
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
     * 课程总数
     */
    private Integer totalCourses;

    /**
     * 目标课程数（个人接口来自 t_employee_training_info 各级别目标数字段）
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
     * 该分类计入个人总分的学分上限。
     * 基础/进阶均为 theory-cap（合计上限，前端合并单元格展示）；
     * 实战为 practical-cap；其它分类为 null。
     */
    private BigDecimal creditCap;

    /**
     * 该分类下已获学分（目标课且已完课的 credit 原始累加，未封顶）
     */
    private BigDecimal earnedCredit;

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

    public BigDecimal getCreditCap() {
        return creditCap;
    }

    public void setCreditCap(BigDecimal creditCap) {
        this.creditCap = creditCap;
    }

    public BigDecimal getEarnedCredit() {
        return earnedCredit;
    }

    public void setEarnedCredit(BigDecimal earnedCredit) {
        this.earnedCredit = earnedCredit;
    }

    public List<CourseInfoVO> getCourseList() {
        return courseList;
    }

    public void setCourseList(List<CourseInfoVO> courseList) {
        this.courseList = courseList;
    }
}
