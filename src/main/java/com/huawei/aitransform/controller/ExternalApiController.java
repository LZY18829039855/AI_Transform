package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.service.CadreDepartmentRefreshService;
import com.huawei.aitransform.service.EmployeeSyncService;
import com.huawei.aitransform.service.EmployeeTrainingInfoSyncService;
import com.huawei.aitransform.service.EntryLevelManagerService;
import com.huawei.aitransform.service.ExpertCertStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    @Autowired
    private CadreDepartmentRefreshService cadreDepartmentRefreshService;

    @Autowired
    private EmployeeSyncService employeeSyncService;

    @Autowired
    private EmployeeTrainingInfoSyncService employeeTrainingInfoSyncService;

    /**
     * 对外开放数据同步更新接口
     * 同步 t_employee_sync 表数据到 t_employee 表
     * 
     * @return 同步结果统计
     */
    @PostMapping("/sync-employee-data")
    public ResponseEntity<Result<java.util.Map<String, Object>>> syncEmployeeData() {
        try {
            // 自动推算period_id：当前日期减2天，格式yyyyMMdd
            LocalDate targetDate = LocalDate.now().minusDays(2);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String periodId = targetDate.format(formatter);
            
            java.util.Map<String, Object> result = employeeSyncService.syncEmployeeData(periodId);
            return ResponseEntity.ok(Result.success("同步成功", result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 同步全体员工训战信息
     * 将指定期号下全部成员与 t_employee_training_info 全量对比，批量新增、更新、删除。
     * 用户基本信息从 t_employee 查询，训战课程字段按四级部门目标课程+完课逻辑刷新。
     *
     * @return 同步结果统计
     */
    @PostMapping("/sync-employee-training-info")
    public ResponseEntity<Result<java.util.Map<String, Object>>> syncEmployeeTrainingInfo() {
        try {
            LocalDate targetDate = LocalDate.now().minusDays(2);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String periodId = targetDate.format(formatter);

            java.util.Map<String, Object> result = employeeTrainingInfoSyncService.syncEmployeeTrainingInfo(periodId);
            return ResponseEntity.ok(Result.success("同步成功", result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

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
     * 同步基层主管（PL/TM/项目经理）数据
     * 
     * 从 t_entry_level_manager_sync 表中筛选出当前有效的PL、TM和项目经理数据，并同步至 t_entry_level_manager 表中。
     * 
     * 业务逻辑：
     * 1. 查询所有状态为有效的PL、TM和项目经理人员（status='Y'）
     * 2. 查询所有任期结束的PL、TM和项目经理（status='N'）
     * 3. 从状态为有效的PL、TM和项目经理中剔除任期结束的数据，得到当前有效的PL、TM和项目经理信息
     * 4. 将有效PL、TM和项目经理的完整数据更新到 t_entry_level_manager 表中
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

    /**
     * 刷新干部部门信息
     * 
     * 从干部表的最小部门ID（mini_departname_id）开始，向上查询父级部门直到二级部门，
     * 并将查询到的部门编码更新到干部表的对应字段中。
     * 
     * 业务逻辑：
     * 1. 查询所有干部及其最小部门ID（mini_departname_id）
     * 2. 对于每个干部，从最小部门ID开始，通过parent_dept_code向上查询父级部门，直到找到二级部门
     * 3. 根据部门级别（dept_level）填充对应的字段：
     *    - l2_department_code：二级部门编码
     *    - l3_department_code：三级部门编码
     *    - l4_department_code：四级部门编码
     *    - l5_department_code：五级部门编码
     * 4. 批量更新干部表的部门编码字段
     * 
     * 注意：干部的最小部门可能是五级、四级或三级，需要向上查询到二级部门为止
     * 
     * @return 刷新结果信息（包含处理的干部数量、成功数量、失败数量等）
     */
    @PostMapping("/refresh-cadre-department-info")
    public ResponseEntity<Result<Object>> refreshCadreDepartmentInfo() {
        try {
            java.util.Map<String, Object> result = cadreDepartmentRefreshService.refreshCadreDepartmentInfo();
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
