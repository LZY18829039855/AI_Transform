package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.service.EntryLevelManagerService;
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

    @Autowired
    private EntryLevelManagerService entryLevelManagerService;

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

    /**
     * 更新L2、L3干部的AI任职达标情况和认证达标情况
     * 
     * 任职要求：
     * - L3干部的AI任职需要达到4+（包括四级），即4级、5级、6级、7级、8级
     * - L2专家的AI任职需要达到3+（包括三级），即3级、4级、5级、6级、7级、8级
     * 如果满足要求，将干部表中的is_qualifications_standard字段更新为1
     * 
     * 认证要求：
     * - 软件类的L2L3干部需要有专业级证书，才算达标，刷新表is_cert_standard字段为1
     * - L2L3的非软件类，需要通过工作级科目二或者专业级科目二，即t_exam_record表中存在exam_code为
     *   （EXCN022303075ZA20，EXCN022303075ZA2E，EXCN022303075ZA2A）且is_pass为1的数据，
     *   如果满足，将is_cert_standard设为1
     * 
     * @return 更新结果信息（包含更新的干部数量）
     */
    @PostMapping("/update-cadre-standard")
    public ResponseEntity<Result<Object>> updateCadreQualificationStandard() {
        try {
            java.util.Map<String, Object> result = expertCertStatisticsService.updateCadreQualificationStandard();
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

    /**
     * 更新L2、L3干部的AI认证达标情况
     * 
     * 认证达标规则：
     * - 所有干部（软件类和非软件类）如果持有AI专业级证书，视为认证达标
     * - 非软件类干部如果通过了专业级科目二考试（exam_code为'EXCN022303075ZA20'、'EXCN022303075ZA2E'或'EXCN022303075ZA2A'之一），视为认证达标
     * 如果满足任一条件，将干部表中的is_cert_standard字段更新为1
     * 
     * @return 更新结果信息（包含更新的干部数量）
     */
    @PostMapping("/update-cadre-cert-standard")
    public ResponseEntity<Result<Object>> updateCadreCertStandard() {
        try {
            java.util.Map<String, Object> result = expertCertStatisticsService.updateCadreCertStandard();
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

    /**
     * 更新L2、L3专家的AI任职达标情况
     * 
     * 任职要求：
     * - L2软件类专家：AI任职需要达到3+（包括三级），即3级、4级、5级、6级、7级、8级
     * - L3级别的所有职位类专家：根据专家的职级判断（职级在t_expert表的orig_position_grade字段）
     *   - 19级专家：要求4+AI任职（4级、5级、6级、7级、8级）
     *   - 20级专家：要求5+AI任职（5级、6级、7级、8级）
     *   - 21级专家：要求6+AI任职（6级、7级、8级）
     *   - 22级专家：要求7+AI任职（7级、8级）
     *   - 23+级专家：要求8级AI任职
     * 如果满足要求，将专家表中的is_qualifications_standard字段更新为1
     * 
     * @return 更新结果信息（包含更新的专家数量）
     */
    @PostMapping("/update-expert-qualification-standard")
    public ResponseEntity<Result<Object>> updateExpertQualificationStandard() {
        try {
            java.util.Map<String, Object> result = expertCertStatisticsService.updateExpertQualificationStandard();
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

    /**
     * 同步基层主管（PL/TM）数据
     * 
     * 从 t_entry_level_manager_sync 表中筛选出当前有效的PL和TM数据，并同步至 t_entry_level_manager 表中。
     * 
     * 业务逻辑：
     * 1. 查询所有状态为有效的PL和TM人员（status='Y'）
     * 2. 查询所有任期结束的PL和TM（status='N'）
     * 3. 从状态为有效的PL和TM中剔除任期结束的数据，得到当前有效的PL和TM信息
     * 4. 将有效PL和TM的完整数据更新到 t_entry_level_manager 表中
     * 
     * @return 同步结果信息（包含同步的数据数量）
     */
    @PostMapping("/sync-entry-level-manager")
    public ResponseEntity<Result<Object>> syncEntryLevelManager() {
        try {
            java.util.Map<String, Object> result = entryLevelManagerService.syncEntryLevelManager();
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
