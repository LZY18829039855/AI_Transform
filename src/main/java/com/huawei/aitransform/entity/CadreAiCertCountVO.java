package com.huawei.aitransform.entity;

import lombok.Data;

/**
 * 干部AI任职认证统计计数VO (用于Mapper返回)
 */
@Data
public class CadreAiCertCountVO {
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
}

