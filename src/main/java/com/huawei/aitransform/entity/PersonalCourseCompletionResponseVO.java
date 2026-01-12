package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 个人课程完成情况响应VO
 */
public class PersonalCourseCompletionResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工工号（不带首字母，如 123456）
     */
    private String empNum;

    /**
     * 员工姓名
     */
    private String empName;

    /**
     * 各训战分类的课程统计列表
     */
    private List<CourseCategoryStatisticsVO> courseStatistics;

    public PersonalCourseCompletionResponseVO() {
    }

    public String getEmpNum() {
        return empNum;
    }

    public void setEmpNum(String empNum) {
        this.empNum = empNum;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public List<CourseCategoryStatisticsVO> getCourseStatistics() {
        return courseStatistics;
    }

    public void setCourseStatistics(List<CourseCategoryStatisticsVO> courseStatistics) {
        this.courseStatistics = courseStatistics;
    }
}

