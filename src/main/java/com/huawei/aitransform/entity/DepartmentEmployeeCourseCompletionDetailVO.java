package com.huawei.aitransform.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 部门全员目标课程完课矩阵（导出用）
 */
@Data
public class DepartmentEmployeeCourseCompletionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<CourseCompletionColumnVO> columns;
    private List<EmployeeCourseCompletionRowVO> rows;
}
