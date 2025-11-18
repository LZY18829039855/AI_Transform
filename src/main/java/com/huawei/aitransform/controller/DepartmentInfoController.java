package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.constant.DepartmentConstants;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.service.DepartmentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门信息控制器
 */
@RestController
@RequestMapping("/department-info")
public class DepartmentInfoController {

    @Autowired
    private DepartmentInfoService departmentInfoService;

    /**
     * 根据部门ID查询下一级部门信息
     * @param deptId 部门ID（部门编码），为空或"0"时默认为云核心网产品线部门ID
     * @return 下一级部门列表（包含部门ID和中文名称）
     */
    @GetMapping("/children")
    public ResponseEntity<Result<List<DepartmentInfoVO>>> getChildDepartments(
            @RequestParam(value = "deptId", required = false) String deptId) {
        try {
            // 如果入参为空或"0"，则使用默认值云核心网产品线部门ID
            if (deptId == null || deptId.trim().isEmpty() || "0".equals(deptId.trim())) {
                deptId = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
            }
            List<DepartmentInfoVO> result = departmentInfoService.getChildDepartments(deptId);
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}

