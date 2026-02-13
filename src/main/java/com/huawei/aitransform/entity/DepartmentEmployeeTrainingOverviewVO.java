package com.huawei.aitransform.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 部门全员训战总览（下钻）单人员 VO
 */
@Data
public class DepartmentEmployeeTrainingOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 姓名 */
    private String name;
    /** 工号 */
    private String employeeNumber;
    /** 职位类 */
    private String jobCategory;
    /** 职位子类 */
    private String jobSubcategory;
    /** 一级部门 */
    private String firstDept;
    /** 二级部门 */
    private String secondDept;
    /** 三级部门 */
    private String thirdDept;
    /** 四级部门 */
    private String fourthDept;
    /** 五级部门 */
    private String fifthDept;
    /** 最小部门 */
    private String lowestDept;
    /** 基础目标课程数 */
    private Integer basicTargetCourseCount;
    /** 基础目标课程完课数 */
    private Integer basicCompletedCount;
    /** 基础目标课程完课占比（百分比） */
    private Double basicCompletionRate;
    /** 进阶目标课程数 */
    private Integer advancedTargetCourseCount;
    /** 进阶目标课程完课数 */
    private Integer advancedCompletedCount;
    /** 进阶目标课程完课占比（百分比） */
    private Double advancedCompletionRate;
    /** 总目标课程数 */
    private Integer totalTargetCourseCount;
    /** 目标课程完课数 */
    private Integer totalCompletedCount;
    /** 目标课程完课占比（百分比） */
    private Double totalCompletionRate;
}
