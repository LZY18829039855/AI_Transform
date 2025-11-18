package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.CompetenceCategoryCertStatisticsResponseVO;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.entity.EmployeeCertCheckRequestVO;
import com.huawei.aitransform.entity.EmployeeCertStatisticsResponseVO;
import com.huawei.aitransform.entity.ExpertCertStatisticsResponseVO;
import com.huawei.aitransform.entity.ExpertCertStatisticsVO;
import com.huawei.aitransform.entity.MaturityCertStatisticsResponseVO;
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
@RequestMapping("/webapi/expert-cert-statistics")
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
     * @param personType 人员类型（0-全员）
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

            // 目前只支持全员（personType=0）
            if (personType != 0) {
                return ResponseEntity.ok(Result.error(400, "暂不支持该人员类型，目前只支持全员（personType=0）"));
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
     * @param personType 人员类型（0-全员）
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

            if (personType == null) {
                return ResponseEntity.ok(Result.error(400, "人员类型不能为空"));
            }

            // 目前只支持全员（personType=0）
            if (personType != 0) {
                return ResponseEntity.ok(Result.error(400, "暂不支持该人员类型，目前只支持全员（personType=0）"));
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
}

