package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.AiCertStatisticsVO;
import com.huawei.aitransform.service.AiCertStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI认证统计控制器
 */
@RestController
@RequestMapping("/ai-cert-statistics")
public class AiCertStatisticsController {

    @Autowired
    private AiCertStatisticsService aiCertStatisticsService;

    /**
     * 统计指定层级部门的AI认证数据
     * @param deptLevel 部门层级（2-4）
     * @param personType 人员类型（0-全员，1-干部，2-专家，3-基层主管）
     * @return 统计结果
     */
    @GetMapping("/statistics")
    public ResponseEntity<Result<List<AiCertStatisticsVO>>> getStatistics(
            @RequestParam(value = "deptLevel", required = true) Integer deptLevel,
            @RequestParam(value = "personType", required = true) Integer personType) {
        
        try {
            // 参数校验
            if (deptLevel == null || deptLevel < 2 || deptLevel > 4) {
                return ResponseEntity.ok(Result.error(400, "部门层级参数必须在2-4之间"));
            }
            
            if (personType == null || personType < 0 || personType > 3) {
                return ResponseEntity.ok(Result.error(400, "人员类型参数必须在0-3之间（0-全员，1-干部，2-专家，3-基层主管）"));
            }

            List<AiCertStatisticsVO> result = aiCertStatisticsService.getAiCertStatistics(deptLevel, personType);
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}

