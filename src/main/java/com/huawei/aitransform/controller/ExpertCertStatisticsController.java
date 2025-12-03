package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.CadreMaturityJobCategoryCertStatisticsResponseVO;
import com.huawei.aitransform.entity.CadreMaturityJobCategoryQualifiedStatisticsResponseVO;
import com.huawei.aitransform.entity.CompetenceCategoryCertStatisticsResponseVO;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.entity.EmployeeCertCheckRequestVO;
import com.huawei.aitransform.entity.EmployeeCertStatisticsResponseVO;
import com.huawei.aitransform.entity.EmployeeDrillDownResponseVO;
import com.huawei.aitransform.entity.ExpertCertStatisticsResponseVO;
import com.huawei.aitransform.entity.ExpertCertStatisticsVO;
import com.huawei.aitransform.entity.MaturityCertStatisticsResponseVO;
import com.huawei.aitransform.constant.DepartmentConstants;
import com.huawei.aitransform.service.ExpertCertStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            
            DepartmentInfoVO deptInfo = 
                expertCertStatisticsService.getDepartmentInfo(deptCode);
            if (deptInfo == null) {
                return ResponseEntity.ok(Result.error(404, "部门不存在：" + deptCode));
            }
            
            List<ExpertCertStatisticsVO> expertList;
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

    /**
     * 通用接口：根据工号列表查询已通过华为研究类能力认证的员工工号
     * @param request 包含员工工号列表的请求对象
     * @return 已通过认证的员工工号列表
     */
    @PostMapping("/check-certification")
    public ResponseEntity<Result<List<String>>> checkEmployeeCertification(
            @RequestBody EmployeeCertCheckRequestVO request) {
        try {
            if (request == null || request.getEmployeeNumbers() == null || request.getEmployeeNumbers().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "员工工号列表不能为空"));
            }

            List<String> certifiedNumbers = expertCertStatisticsService.getCertifiedEmployeeNumbers(
                    request.getEmployeeNumbers());
            return ResponseEntity.ok(Result.success("查询成功", certifiedNumbers));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 通用接口：根据工号列表查询获得AI任职的员工工号
     * @param request 包含员工工号列表的请求对象
     * @return 获得AI任职的员工工号列表
     */
    @PostMapping("/check-qualification")
    public ResponseEntity<Result<List<String>>> checkEmployeeQualification(
            @RequestBody EmployeeCertCheckRequestVO request) {
        try {
            if (request == null || request.getEmployeeNumbers() == null || request.getEmployeeNumbers().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "员工工号列表不能为空"));
            }

            List<String> qualifiedNumbers = expertCertStatisticsService.getQualifiedEmployeeNumbers(
                    request.getEmployeeNumbers());
            return ResponseEntity.ok(Result.success("查询成功", qualifiedNumbers));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 快速查询接口：查询单个员工是否通过认证（GET方式，方便测试）
     * @param employeeNumber 员工工号
     * @return 是否通过认证（true/false）
     */
    @GetMapping("/check-single")
    public ResponseEntity<Result<Boolean>> checkSingleEmployee(
            @RequestParam(value = "employeeNumber", required = true) String employeeNumber) {
        try {
            if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "员工工号不能为空"));
            }

            List<String> employeeNumbers = new java.util.ArrayList<>();
            employeeNumbers.add(employeeNumber);
            List<String> certifiedNumbers = expertCertStatisticsService.getCertifiedEmployeeNumbers(employeeNumbers);
            boolean isCertified = certifiedNumbers != null && certifiedNumbers.contains(employeeNumber);
            return ResponseEntity.ok(Result.success("查询成功", isCertified));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 查询全员任职认证信息
     * @param deptCode 部门ID（部门编码）
     * @param personType 人员类型（0-全员，1-干部）
     * @return 认证和任职统计信息（包含各部门统计和总计，包含认证人数和任职人数）
     */
    @GetMapping("/employee-cert-statistics")
    public ResponseEntity<Result<EmployeeCertStatisticsResponseVO>> getEmployeeCertStatistics(
            @RequestParam(value = "deptCode", required = true) String deptCode,
            @RequestParam(value = "personType", required = true) Integer personType) {
        try {
            if (deptCode == null || deptCode.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "部门ID不能为空"));
            }

            if (personType == null) {
                return ResponseEntity.ok(Result.error(400, "人员类型不能为空"));
            }

            // 支持全员（personType=0）和干部（personType=1）
            if (personType != 0 && personType != 1) {
                return ResponseEntity.ok(Result.error(400, "暂不支持该人员类型，目前只支持全员（personType=0）和干部（personType=1）"));
            }

            EmployeeCertStatisticsResponseVO result = expertCertStatisticsService.getEmployeeCertStatistics(deptCode, personType);
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 按职位类统计部门下不同职位类人数中的认证和任职人数
     * @param deptCode 部门ID（部门编码）
     * @param personType 人员类型（0-全员，1-干部）
     * @return 按职位类统计的认证和任职信息（包含认证人数和任职人数）
     */
    @GetMapping("/competence-category-cert-statistics")
    public ResponseEntity<Result<CompetenceCategoryCertStatisticsResponseVO>> getCompetenceCategoryCertStatistics(
            @RequestParam(value = "deptCode", required = true) String deptCode,
            @RequestParam(value = "personType", required = true) Integer personType) {
        try {
            if (deptCode == null || deptCode.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "部门ID不能为空"));
            }

            // 当deptCode为"0"时，使用云核心网产品线部门ID
            if ("0".equals(deptCode.trim())) {
                deptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
            }

            if (personType == null) {
                return ResponseEntity.ok(Result.error(400, "人员类型不能为空"));
            }

            // 支持全员（personType=0）和干部（personType=1）
            if (personType != 0 && personType != 1) {
                return ResponseEntity.ok(Result.error(400, "暂不支持该人员类型，目前只支持全员（personType=0）和干部（personType=1）"));
            }

            CompetenceCategoryCertStatisticsResponseVO result = expertCertStatisticsService.getCompetenceCategoryCertStatistics(deptCode, personType);
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 按组织成熟度统计通过认证和任职的人数
     * @param deptCode 部门ID（部门编码）
     * @param personType 人员类型（0-全员）
     * @return 按成熟度统计的认证和任职信息（包含认证人数和任职人数）
     */
    @GetMapping("/maturity-cert-statistics")
    public ResponseEntity<Result<MaturityCertStatisticsResponseVO>> getMaturityCertStatistics(
            @RequestParam(value = "deptCode", required = true) String deptCode,
            @RequestParam(value = "personType", required = true) Integer personType) {
        try {
            if (deptCode == null || deptCode.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "部门ID不能为空"));
            }

            // 当deptCode为"0"时，使用研发管理部部门ID
            if ("0".equals(deptCode.trim())) {
                deptCode = DepartmentConstants.R_D_MANAGEMENT_DEPT_CODE;
            }

            if (personType == null) {
                return ResponseEntity.ok(Result.error(400, "人员类型不能为空"));
            }

            // 目前只支持全员（personType=0）
            if (personType != 0) {
                return ResponseEntity.ok(Result.error(400, "暂不支持该人员类型，目前只支持全员（personType=0）"));
            }

            MaturityCertStatisticsResponseVO result = expertCertStatisticsService.getMaturityCertStatistics(deptCode, personType);
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 查询部门维度的下钻信息
     * @param deptCode 部门ID（部门编码）
     * @param personType 人员类型（0：全员数据）
     * @param dataType 数据类型（1：任职数据，2：认证数据）
     * @return 员工详细信息列表
     */
    @GetMapping("/employee-drill-down")
    public ResponseEntity<Result<EmployeeDrillDownResponseVO>> getEmployeeDrillDownInfo(
            @RequestParam(value = "deptCode", required = true) String deptCode,
            @RequestParam(value = "personType", required = true) Integer personType,
            @RequestParam(value = "dataType", required = true) Integer dataType) {
        try {
            if (deptCode == null || deptCode.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "部门ID不能为空"));
            }

            if (personType == null) {
                return ResponseEntity.ok(Result.error(400, "人员类型不能为空"));
            }

            if (dataType == null) {
                return ResponseEntity.ok(Result.error(400, "数据类型不能为空"));
            }

            EmployeeDrillDownResponseVO result = expertCertStatisticsService.getEmployeeDrillDownInfo(deptCode, personType, dataType);
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 查询干部任职认证数据（按成熟度和职位类统计）
     * @param deptCode 部门ID（部门编码）
     * @return 干部成熟度职位类认证统计信息
     */
    @GetMapping("/cadre-cert-statistics/by-maturity-and-job-category")
    public ResponseEntity<Result<CadreMaturityJobCategoryCertStatisticsResponseVO>> getCadreMaturityJobCategoryCertStatistics(
            @RequestParam(value = "deptCode", required = true) String deptCode) {
        try {
            if (deptCode == null || deptCode.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "部门ID不能为空"));
            }

            CadreMaturityJobCategoryCertStatisticsResponseVO result = expertCertStatisticsService.getCadreMaturityJobCategoryCertStatistics(deptCode);
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 查询干部任职数据（按成熟度和职位类统计，仅L2和L3）
     * @param deptCode 部门ID（部门编码），当为"0"时，Service层会处理
     * @return 干部成熟度职位类任职统计信息
     */
    @GetMapping("/cadre-cert-statistics/by-maturity-and-job-category-qualified")
    public ResponseEntity<Result<CadreMaturityJobCategoryQualifiedStatisticsResponseVO>> getCadreMaturityJobCategoryQualifiedStatistics(
            @RequestParam(value = "deptCode", required = false) String deptCode) {
        try {
            if (deptCode == null || deptCode.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "部门ID不能为空"));
            }

            CadreMaturityJobCategoryQualifiedStatisticsResponseVO result = expertCertStatisticsService.getCadreMaturityJobCategoryQualifiedStatistics(deptCode);
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 查询干部或专家认证类信息（默认查询认证数据）
     * @param deptCode 部门ID（部门编码），当为"0"或为空时，默认查询云核心网产品线部门ID
     * @param aiMaturity 岗位AI成熟度（L5代表查询L2和L3的数据）
     * @param jobCategory 职位类
     * @param personType 人员类型（1-干部，2-专家）
     * @param queryType 查询类型（1-任职人数，2-基线人数），默认为1（任职人数），仅对干部类型有效
     * @return 员工详细信息列表
     */
    @GetMapping("/person-cert-details")
    public ResponseEntity<Result<EmployeeDrillDownResponseVO>> getPersonCertDetailsByConditions(
            @RequestParam(value = "deptCode", required = true) String deptCode,
            @RequestParam(value = "aiMaturity", required = false) String aiMaturity,
            @RequestParam(value = "jobCategory", required = false) String jobCategory,
            @RequestParam(value = "personType", required = true) Integer personType,
            @RequestParam(value = "queryType", required = false, defaultValue = "1") Integer queryType) {
        try {
            // 当deptCode为"0"、空字符串或未提供时，使用默认值云核心网产品线部门ID
            if (deptCode == null || deptCode.trim().isEmpty() || "0".equals(deptCode.trim())) {
                deptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
            }

            if (personType == null) {
                return ResponseEntity.ok(Result.error(400, "人员类型不能为空"));
            }

            // 验证 queryType 参数
            if (queryType != null && queryType != 1 && queryType != 2) {
                return ResponseEntity.ok(Result.error(400, "查询类型参数错误，只支持1（任职人数）或2（基线人数）"));
            }

            // 如果未提供 queryType，默认为1（任职人数）
            if (queryType == null) {
                queryType = 1;
            }

            EmployeeDrillDownResponseVO result = expertCertStatisticsService.getPersonCertDetailsByConditions(
                    deptCode, aiMaturity, jobCategory, personType, queryType);
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 查询干部任职数据
     * @param deptCode 部门ID（部门编码），当为"0"或为空时，默认查询云核心网产品线部门ID
     * @param aiMaturity 岗位AI成熟度
     * @param jobCategory 职位类
     * @param personType 人员类型（1-干部，当前只处理干部类型）
     * @param queryType 查询类型（1-任职人数，2-基线人数），默认为1（任职人数）
     * @return 员工详细信息列表
     */
    @GetMapping("/cadre-qualified-details")
    public ResponseEntity<Result<EmployeeDrillDownResponseVO>> getCadreQualifiedDetailsByConditions(
            @RequestParam(value = "deptCode", required = true) String deptCode,
            @RequestParam(value = "aiMaturity", required = false) String aiMaturity,
            @RequestParam(value = "jobCategory", required = false) String jobCategory,
            @RequestParam(value = "personType", required = true) Integer personType,
            @RequestParam(value = "queryType", required = false, defaultValue = "1") Integer queryType) {
        try {
            // 当deptCode为"0"、空字符串或未提供时，使用默认值云核心网产品线部门ID
            if (deptCode == null || deptCode.trim().isEmpty() || "0".equals(deptCode.trim())) {
                deptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
            }

            if (personType == null) {
                return ResponseEntity.ok(Result.error(400, "人员类型不能为空"));
            }

            // 验证 queryType 参数
            if (queryType != null && queryType != 1 && queryType != 2) {
                return ResponseEntity.ok(Result.error(400, "查询类型参数错误，只支持1（任职人数）或2（基线人数）"));
            }

            // 如果未提供 queryType，默认为1（任职人数）
            if (queryType == null) {
                queryType = 1;
            }

            EmployeeDrillDownResponseVO result = expertCertStatisticsService.getCadreQualifiedDetailsByConditions(
                    deptCode, aiMaturity, jobCategory, personType, queryType);
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 更新L2、L3干部的AI任职达标情况和认证达标情况
     * 
     * 任职要求：
     * - L3干部的AI任职需要达到4+（不包括四级），即5级、6级、7级、8级
     * - L2专家的AI任职需要达到3+（不包括3级），即4级、5级、6级、7级、8级
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
}

