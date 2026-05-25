package com.huawei.aitransform.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 部门完课矩阵导出：单行人员数据
 */
@Data
public class EmployeeCourseCompletionRowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String employeeNumber;
    private String name;
    private String firstDept;
    private String secondDept;
    private String thirdDept;
    private String fourthDept;
    private String fifthDept;
    private String lowestDept;
    /** 列 key -> 是否完课（仅目标课程列有 true，未完课或未纳入目标则为 false） */
    private Map<String, Boolean> completions;
}
