package com.huawei.aitransform.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 个人科目二通过明细（用于任职认证详情展示）
 */
@Data
public class Subject2ExamDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 科目二考试名称 */
    private String examName;

    /** 计入学分的分值（认证未通过且科目二通过时为 5） */
    private Integer credit;
}
