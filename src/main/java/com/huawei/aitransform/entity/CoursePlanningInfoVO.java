package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * AI课程规划明细表VO
 */
public class CoursePlanningInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 大类
     */
    private String bigType;

    /**
     * 子类
     */
    private String sybType;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程链接
     */
    private String courseLink;

    /**
     * 学分
     */
    private String credit;

    /**
     * 课程状态
     */
    private String courseStatus;

    /**
     * 知识点
     */
    private String knowledgePoint;

    /**
     * 课程说明
     */
    private String courseExplain;

    /**
     * 学习时长
     */
    private String studyDuration;

    /**
     * 课程级别
     */
    private String courseLevel;

    /**
     * 随堂测试
     */
    private String inClassTest;

    public CoursePlanningInfoVO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBigType() {
        return bigType;
    }

    public void setBigType(String bigType) {
        this.bigType = bigType;
    }

    public String getSybType() {
        return sybType;
    }

    public void setSybType(String sybType) {
        this.sybType = sybType;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseLink() {
        return courseLink;
    }

    public void setCourseLink(String courseLink) {
        this.courseLink = courseLink;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getCourseStatus() {
        return courseStatus;
    }

    public void setCourseStatus(String courseStatus) {
        this.courseStatus = courseStatus;
    }

    public String getKnowledgePoint() {
        return knowledgePoint;
    }

    public void setKnowledgePoint(String knowledgePoint) {
        this.knowledgePoint = knowledgePoint;
    }

    public String getCourseExplain() {
        return courseExplain;
    }

    public void setCourseExplain(String courseExplain) {
        this.courseExplain = courseExplain;
    }

    public String getStudyDuration() {
        return studyDuration;
    }

    public void setStudyDuration(String studyDuration) {
        this.studyDuration = studyDuration;
    }

    public String getCourseLevel() {
        return courseLevel;
    }

    public void setCourseLevel(String courseLevel) {
        this.courseLevel = courseLevel;
    }

    public String getInClassTest() {
        return inClassTest;
    }

    public void setInClassTest(String inClassTest) {
        this.inClassTest = inClassTest;
    }
}


