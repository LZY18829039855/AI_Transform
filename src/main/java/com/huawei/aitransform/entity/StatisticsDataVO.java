package com.huawei.aitransform.entity;

import java.io.Serializable;

/**
 * 统计数据VO（用于PL/TM或PM的统计数据）
 */
public class StatisticsDataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总人数
     */
    private Integer totalCount;

    /**
     * 通过任职标准的人数（is_qualifications_standard=1）
     */
    private Integer qualifiedCount;

    /**
     * 任职占比（qualifiedCount/totalCount，保留4位小数）
     */
    private Double qualifiedRatio;

    /**
     * 通过认证标准的人数（is_cert_standard=1）
     */
    private Integer certCount;

    /**
     * 认证占比（certCount/totalCount，保留4位小数）
     */
    private Double certRatio;

    public StatisticsDataVO() {
    }

    public StatisticsDataVO(Integer totalCount, Integer qualifiedCount, Double qualifiedRatio, Integer certCount, Double certRatio) {
        this.totalCount = totalCount;
        this.qualifiedCount = qualifiedCount;
        this.qualifiedRatio = qualifiedRatio;
        this.certCount = certCount;
        this.certRatio = certRatio;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getQualifiedCount() {
        return qualifiedCount;
    }

    public void setQualifiedCount(Integer qualifiedCount) {
        this.qualifiedCount = qualifiedCount;
    }

    public Double getQualifiedRatio() {
        return qualifiedRatio;
    }

    public void setQualifiedRatio(Double qualifiedRatio) {
        this.qualifiedRatio = qualifiedRatio;
    }

    public Integer getCertCount() {
        return certCount;
    }

    public void setCertCount(Integer certCount) {
        this.certCount = certCount;
    }

    public Double getCertRatio() {
        return certRatio;
    }

    public void setCertRatio(Double certRatio) {
        this.certRatio = certRatio;
    }
}

