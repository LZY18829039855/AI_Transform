package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * L2/L3干部统计数据VO
 */
public class L2L3StatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * L2/L3干部岗位总数
     */
    private Integer totalCount;

    /**
     * 软件类数量
     */
    private Integer softwareCount;

    /**
     * 非软件类数量
     */
    private Integer nonSoftwareCount;

    public L2L3StatisticsVO() {
    }

    public L2L3StatisticsVO(Integer totalCount, Integer softwareCount, Integer nonSoftwareCount) {
        this.totalCount = totalCount;
        this.softwareCount = softwareCount;
        this.nonSoftwareCount = nonSoftwareCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getSoftwareCount() {
        return softwareCount;
    }

    public void setSoftwareCount(Integer softwareCount) {
        this.softwareCount = softwareCount;
    }

    public Integer getNonSoftwareCount() {
        return nonSoftwareCount;
    }

    public void setNonSoftwareCount(Integer nonSoftwareCount) {
        this.nonSoftwareCount = nonSoftwareCount;
    }
}

