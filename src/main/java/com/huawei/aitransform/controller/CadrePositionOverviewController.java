package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.CadrePositionOverviewResponseVO;
import com.huawei.aitransform.service.CadrePositionOverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 干部岗位概述统计Controller
 */
@RestController
@RequestMapping("/cadre-cert-statistics")
public class CadrePositionOverviewController {

    @Autowired
    private CadrePositionOverviewService cadrePositionOverviewService;

    /**
     * 统计云核心网产品线下面所有三级部门以及研发管理部下面所有四级部门的干部总岗位数，
     * 以及L2/L3干部岗位总数、L2/L3干部岗位占比，以及L2干部中软件类与非软件类的数量，L3干部下面软件类与非软件类的数量。
     */
    @GetMapping("/cadre-position-overview")
    public Result<CadrePositionOverviewResponseVO> getCadrePositionOverview() {
        return Result.success(cadrePositionOverviewService.getCadrePositionOverview());
    }
}

