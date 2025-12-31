package com.huawei.aitransform.entity;

import lombok.Data;

@Data
public class EmployeePO {
    private String employeeNumber;
    private String lastName;

    private String firstdeptcode;
    private String seconddeptcode;
    private String thirddeptcode;
    private String fourthdeptcode;
    private String fifthdeptcode;
    private String sixthdeptcode;

    private String lowestDeptId;

    private String firstdept;
    private String seconddept;
    private String thirddept;
    private String fourthdept;
    private String fifthdept;
    private String sixthdept;
    
    private String lowestDept;

    private String jobType;

    private String jobCategory;

    private String jobSubcategory;

    private String periodId;

    private String updatedTime;

    private Integer isQualificationsStandard;

    private Integer isCertStandard;

    private String certTitle;

    private Integer isPassedSubject2;

    private String competenceFamilyCn;

    private String competenceCategoryCn;

    private String competenceSubcategoryCn;

    private String directionCnName;

    private String competenceRatingCn;

    private String competenceGradeCn;

    private String competenceFrom;

    private String competenceTo;
}

