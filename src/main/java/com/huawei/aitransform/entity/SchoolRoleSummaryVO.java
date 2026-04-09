package com.huawei.aitransform.entity;

public class SchoolRoleSummaryVO {
    private String maturityLevel;
    private Integer baseline;
    private Double maxCredits;
    private Double minCredits;
    private Double averageCredits;
    private Double targetCredits;
    private Double completionRate;
    private Double scheduleTarget;
    private String status;
    private String statusType;

    public String getMaturityLevel() { return maturityLevel; }
    public void setMaturityLevel(String maturityLevel) { this.maturityLevel = maturityLevel; }
    public Integer getBaseline() { return baseline; }
    public void setBaseline(Integer baseline) { this.baseline = baseline; }
    public Double getMaxCredits() { return maxCredits; }
    public void setMaxCredits(Double maxCredits) { this.maxCredits = maxCredits; }
    public Double getMinCredits() { return minCredits; }
    public void setMinCredits(Double minCredits) { this.minCredits = minCredits; }
    public Double getAverageCredits() { return averageCredits; }
    public void setAverageCredits(Double averageCredits) { this.averageCredits = averageCredits; }
    public Double getTargetCredits() { return targetCredits; }
    public void setTargetCredits(Double targetCredits) { this.targetCredits = targetCredits; }
    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
    public Double getScheduleTarget() { return scheduleTarget; }
    public void setScheduleTarget(Double scheduleTarget) { this.scheduleTarget = scheduleTarget; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStatusType() { return statusType; }
    public void setStatusType(String statusType) { this.statusType = statusType; }
}