package com.huawei.aitransform.entity;

import java.util.List;

public class SchoolRoleSummaryResponseVO {
    private List<SchoolRoleSummaryVO> expertSummary;
    private List<SchoolRoleSummaryVO> cadreSummary;

    public List<SchoolRoleSummaryVO> getExpertSummary() { return expertSummary; }
    public void setExpertSummary(List<SchoolRoleSummaryVO> expertSummary) { this.expertSummary = expertSummary; }
    public List<SchoolRoleSummaryVO> getCadreSummary() { return cadreSummary; }
    public void setCadreSummary(List<SchoolRoleSummaryVO> cadreSummary) { this.cadreSummary = cadreSummary; }
}