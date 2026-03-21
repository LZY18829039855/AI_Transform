package com.huawei.aitransform.entity;

import lombok.Data;

/**
 * 全员训战课程信息表 t_employee_training_info 实体
 */
@Data
public class EmployeeTrainingInfoPO {
    private String employeeNumber;
    private String lastName;
    private String firstdeptcode;
    private String seconddeptcode;
    private String thirddeptcode;
    private String fourthdeptcode;
    private String fifthdeptcode;
    private String sixthdeptcode;
    private String lowestdeptid;
    private String firstdept;
    private String seconddept;
    private String thirddept;
    private String fourthdept;
    private String fifthdept;
    private String sixthdept;
    private String lowestdept;
    private String jobType;
    private String jobCategory;
    private String jobSubcategory;
    private String periodId;
    private String updatedTime;
    /** 基础目标课程完课列表（逗号分隔） */
    private String basicCourses;
    /** 进阶目标课程完课列表（逗号分隔） */
    private String advancedCourses;
    /** 实战目标课程完课列表（逗号分隔） */
    private String practicalCourses;
    /** 基础目标课程数 */
    private Integer basicTargetCoursesNum;
    /** 进阶目标课程数 */
    private Integer advancedTargetCoursesNum;
    /** 实战目标课程数 */
    private Integer practicalTargetCoursesNum;

    /** 干部岗位 AI 成熟度（同步自 t_cadre.position_ai_maturity） */
    private String cadrePositionAiMaturity;

    /** 专家岗位 AI 成熟度（同步自 t_expert.position_ai_maturity） */
    private String expertPositionAiMaturity;
}
