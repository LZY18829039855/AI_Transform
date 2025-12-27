package com.huawei.aitransform.entity;

import lombok.Data;
import java.util.List;

/**
 * AI干部任职认证统计VO
 */
@Data
public class CadreAiCertStatisticsVO {
    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门层级
     */
    private String deptLevel;

    /**
     * 干部总人数
     */
    private Integer totalCadreCount;

    /**
     * L2L3人数
     */
    private Integer l2L3Count;

    /**
     * 软件L2人数
     */
    private Integer softwareL2Count;

    /**
     * 软件L3人数
     */
    private Integer softwareL3Count;

    /**
     * 非软件L2L3人数
     */
    private Integer nonSoftwareL2L3Count;

    /**
     * 满足岗位AI要求的L2L3人数
     */
    private Integer qualifiedL2L3Count;

    /**
     * 满足岗位AI要求的L2L3干部占比
     */
    private Double qualifiedL2L3Ratio;

    /**
     * 子部门列表
     */
    private List<CadreAiCertStatisticsVO> children;
}

