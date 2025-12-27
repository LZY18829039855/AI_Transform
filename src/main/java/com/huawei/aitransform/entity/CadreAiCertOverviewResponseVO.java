package com.huawei.aitransform.entity;

import lombok.Data;
import java.util.List;

/**
 * AI干部任职认证概览响应VO
 */
@Data
public class CadreAiCertOverviewResponseVO {
    /**
     * 汇总数据
     */
    private CadreAiCertStatisticsVO summary;

    /**
     * 部门列表
     */
    private List<CadreAiCertStatisticsVO> departmentList;
}

