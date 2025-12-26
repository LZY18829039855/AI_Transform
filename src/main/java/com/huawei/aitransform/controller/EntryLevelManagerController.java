package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.PlTmCertStatisticsResponseVO;
import com.huawei.aitransform.service.EntryLevelManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基层主管（PL/TM）数据控制器
 */
@RestController
@RequestMapping("/entry-level-manager")
public class EntryLevelManagerController {

    @Autowired
    private EntryLevelManagerService entryLevelManagerService;

    /**
     * 查询PL、TM、PM（项目经理）任职与认证数据
     * 按部门维度返回，每个部门包含PL/TM和PM两套统计数据
     * PL和TM合并统计，PM单独统计
     * 统计研发管理部下各四级部门以及研发管理部整体的PL/TM总人数、PM总人数、通过任职标准的人数及占比、通过认证标准的人数及占比
     * 
     * @return PL/TM/PM任职与认证统计数据
     */
    @GetMapping("/pl-tm-cert-statistics")
    public ResponseEntity<Result<PlTmCertStatisticsResponseVO>> getPlTmCertStatistics() {
        try {
            PlTmCertStatisticsResponseVO result = entryLevelManagerService.getPlTmCertStatistics();
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}

