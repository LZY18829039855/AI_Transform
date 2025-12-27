package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 干部统计计数VO
 */
public class CadreStatisticsCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 干部总数
     */
    private Integer totalCount;

    /**
     * L2软件类数量
     */
    private Integer l2SoftwareCount;

    /**
     * L2非软件类数量
     */
    private Integer l2NonSoftwareCount;

    /**
     * L3软件类数量
     */
    private Integer l3SoftwareCount;

    /**
     * L3非软件类数量
     */
    private Integer l3NonSoftwareCount;

    public CadreStatisticsCountVO() {
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getL2SoftwareCount() {
        return l2SoftwareCount;
    }

    public void setL2SoftwareCount(Integer l2SoftwareCount) {
        this.l2SoftwareCount = l2SoftwareCount;
    }

    public Integer getL2NonSoftwareCount() {
        return l2NonSoftwareCount;
    }

    public void setL2NonSoftwareCount(Integer l2NonSoftwareCount) {
        this.l2NonSoftwareCount = l2NonSoftwareCount;
    }

    public Integer getL3SoftwareCount() {
        return l3SoftwareCount;
    }

    public void setL3SoftwareCount(Integer l3SoftwareCount) {
        this.l3SoftwareCount = l3SoftwareCount;
    }

    public Integer getL3NonSoftwareCount() {
        return l3NonSoftwareCount;
    }

    public void setL3NonSoftwareCount(Integer l3NonSoftwareCount) {
        this.l3NonSoftwareCount = l3NonSoftwareCount;
    }
}

