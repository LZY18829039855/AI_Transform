package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.service.ExpertCertStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 外部接口控制器
 * 用于开放给外部系统调用的接口
 */
@RestController
@RequestMapping("/external-api")
public class ExternalApiController {

    @Autowired
    private ExpertCertStatisticsService expertCertStatisticsService;

    /**
     * 更新L2、L3专家的认证达标情况
     * 
     * 认证达标规则：
     * - 所有L2、L3专家（所有职位类）：如果持有专业级证书即为达标，否则视为不达标
     * 如果满足条件，将专家表中的is_cert_standard字段更新为1，不达标为0
     * 
     * @return 更新结果信息（包含更新的专家数量）
     */
    @PostMapping("/update-expert-cert-standard")
    public ResponseEntity<Result<Object>> updateExpertCertStandard() {
        try {
            java.util.Map<String, Object> result = expertCertStatisticsService.updateExpertCertStandard();
            Boolean success = (Boolean) result.get("success");
            if (success != null && success) {
                return ResponseEntity.ok(Result.success((String) result.get("message"), result));
            } else {
                return ResponseEntity.ok(Result.error(500, (String) result.get("message")));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

}
