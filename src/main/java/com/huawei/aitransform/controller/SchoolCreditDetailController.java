package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.SchoolCreditDetailRequestVO;
import com.huawei.aitransform.entity.SchoolCreditDetailResponseVO;
import com.huawei.aitransform.service.SchoolCreditDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI School学分数据明细查询Controller
 * 用于处理AI School看板基线人数点击下钻的学分数据明细查询
 */
@RestController
@RequestMapping("/api/school-credit-detail")
public class SchoolCreditDetailController {

    @Autowired
    private SchoolCreditDetailService schoolCreditDetailService;

    /**
     * 查询AI School学分数据明细
     * 用于基线人数点击下钻场景
     *
     * @param request 查询请求参数
     * @return 学分数据明细列表（分页）
     */
    @PostMapping("/list")
    public ResponseEntity<Result<SchoolCreditDetailResponseVO>> getCreditDetailList(
            @RequestBody SchoolCreditDetailRequestVO request) {
        try {
            // 参数校验
            if (request.getDeptCode() == null || request.getDeptCode().trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "部门编码不能为空"));
            }

            SchoolCreditDetailResponseVO response = schoolCreditDetailService.getCreditDetailList(request);
            return ResponseEntity.ok(Result.success("success", response));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "查询失败：" + e.getMessage()));
        }
    }

    /**
     * 根据部门查询学分数据明细（GET方式，简化调用）
     *
     * @param deptCode 部门编码
     * @param deptLevel 部门层级（可选，默认0）
     * @param roleType 角色类型（可选，默认0全员）
     * @param jobFamily 职位族（可选）
     * @param jobCategory 职位类（可选）
     * @param jobSubCategory 职位子类（可选）
     * @param positionMaturity 岗位成熟度（可选）
     * @param pageNum 页码（可选，默认1）
     * @param pageSize 每页大小（可选，默认50）
     * @return 学分数据明细列表
     */
    @GetMapping("/list")
    public ResponseEntity<Result<SchoolCreditDetailResponseVO>> getCreditDetailListByDept(
            @RequestParam("deptCode") String deptCode,
            @RequestParam(value = "deptLevel", required = false, defaultValue = "-1") Integer deptLevel,
            @RequestParam(value = "roleType", required = false, defaultValue = "0") Integer roleType,
            @RequestParam(value = "jobFamily", required = false) String jobFamily,
            @RequestParam(value = "jobCategory", required = false) String jobCategory,
            @RequestParam(value = "jobSubCategory", required = false) String jobSubCategory,
            @RequestParam(value = "positionMaturity", required = false) String positionMaturity,
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "50") Integer pageSize) {
        try {
            SchoolCreditDetailRequestVO request = new SchoolCreditDetailRequestVO();
            request.setDeptCode(deptCode);
            request.setDeptLevel(deptLevel);
            request.setRoleType(roleType);
            request.setJobFamily(jobFamily);
            request.setJobCategory(jobCategory);
            request.setJobSubCategory(jobSubCategory);
            request.setPositionMaturity(positionMaturity);
            request.setPageNum(pageNum);
            request.setPageSize(pageSize);

            SchoolCreditDetailResponseVO response = schoolCreditDetailService.getCreditDetailList(request);
            return ResponseEntity.ok(Result.success("success", response));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "查询失败：" + e.getMessage()));
        }
    }
}
