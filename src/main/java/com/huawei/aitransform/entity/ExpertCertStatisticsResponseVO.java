package com.huawei.aitransform.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 专家认证统计响应VO
 */
public class ExpertCertStatisticsResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 按AI成熟度统计的数据（树形结构，包含职位类统计）
     */
    private List<ExpertCertStatisticsVO> maturityStatistics;

    public ExpertCertStatisticsResponseVO() {
    }

    public List<ExpertCertStatisticsVO> getMaturityStatistics() {
        return maturityStatistics;
    }

    public void setMaturityStatistics(List<ExpertCertStatisticsVO> maturityStatistics) {
        this.maturityStatistics = maturityStatistics;
    }
}

