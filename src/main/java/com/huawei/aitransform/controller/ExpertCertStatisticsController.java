package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.ExpertCertStatisticsResponseVO;
import com.huawei.aitransform.service.ExpertCertStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 专家认证统计控制器
 */
@RestController
@RequestMapping("/expert-cert-statistics")
public class ExpertCertStatisticsController {

    @Autowired
    private ExpertCertStatisticsService expertCertStatisticsService;

    /**
     * 查询专家任职认证数据
     * @param deptCode 部门ID（部门编码）
     * @return 统计结果
     */
    @GetMapping("/statistics")
    public ResponseEntity<Result<ExpertCertStatisticsResponseVO>> getExpertCertStatistics(
            @RequestParam(value = "deptCode", required = true) String deptCode) {
        
        try {
            if (deptCode == null || deptCode.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "部门ID不能为空"));
            }

            ExpertCertStatisticsResponseVO result = expertCertStatisticsService.getExpertCertStatistics(deptCode);
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 调试接口：查看专家原始数据（包含hasCert字段）
     * @param deptCode 部门编码
     * @return 专家原始数据列表
     */
    @GetMapping("/debug")
    public ResponseEntity<Result<Object>> getExpertDebugInfo(
            @RequestParam(value = "deptCode", required = true) String deptCode) {
        try {
            if (deptCode == null || deptCode.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "部门ID不能为空"));
            }
            
            com.huawei.aitransform.entity.DepartmentInfoVO deptInfo = 
                expertCertStatisticsService.getDepartmentInfo(deptCode);
            if (deptInfo == null) {
                return ResponseEntity.ok(Result.error(404, "部门不存在：" + deptCode));
            }
            
            List<com.huawei.aitransform.entity.ExpertCertStatisticsVO> expertList;
            if ("3".equals(deptInfo.getDeptLevel())) {
                expertList = expertCertStatisticsService.getExpertListByLevel3(deptCode, deptInfo.getDeptName());
            } else if ("4".equals(deptInfo.getDeptLevel())) {
                expertList = expertCertStatisticsService.getExpertListByLevel4(deptCode);
            } else {
                return ResponseEntity.ok(Result.error(400, "只支持查询三层或四层部门"));
            }
            
            return ResponseEntity.ok(Result.success("查询成功", expertList));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}

