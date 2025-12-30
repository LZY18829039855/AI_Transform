package com.huawei.aitransform.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EmployeeSyncDataVO {
    @JsonProperty("employee_number")
    private String employeeNumber;

    @JsonProperty("last_name")
    private String lastName;

    private String firstdeptcode;
    private String seconddeptcode;
    private String thirddeptcode;
    private String fourthdeptcode;
    private String fifthdeptcode;
    private String sixthdeptcode;

    @JsonProperty("lowestdeptid")
    private String lowestDeptNumber;

    private String firstdept;
    private String seconddept;
    private String thirddept;
    private String fourthdept;
    private String fifthdept;
    private String sixthdept;
    
    @JsonProperty("lowestdept")
    private String lowestDept;

    @JsonProperty("job_type")
    private String jobType;

    @JsonProperty("job_category")
    private String jobCategory;

    @JsonProperty("job_subcategory")
    private String jobSubcategory;

    @JsonProperty("period_id")
    private String periodId;

    @JsonProperty("updated_time")
    private String updatedTime;

    @JsonProperty("is_qualifications_standard")
    private Integer isQualificationsStandard;

    @JsonProperty("is_cert_standard")
    private Integer isCertStandard;

    @JsonProperty("cert_title")
    private String certTitle;

    @JsonProperty("is_passed_subject2")
    private Integer isPassedSubject2;

    @JsonProperty("competence_family_cn")
    private String competenceFamilyCn;

    @JsonProperty("competence_category_cn")
    private String competenceCategoryCn;

    @JsonProperty("competence_subcategory_cn")
    private String competenceSubcategoryCn;

    @JsonProperty("direction_cn_name")
    private String directionCnName;

    @JsonProperty("competence_rating_cn")
    private String competenceRatingCn;

    @JsonProperty("competence_from")
    private String competenceFrom;

    @JsonProperty("competence_to")
    private String competenceTo;
}

