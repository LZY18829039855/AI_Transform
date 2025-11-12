package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.service.DepartmentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * 查询云核心网产品线及其所有下属部门信息（树形结构）
     * @return 部门信息树形结构
     */
    @GetMapping("/list")
    public ResponseEntity<Result<DepartmentInfoVO>> getDepartmentInfoList() {
        try {
            DepartmentInfoVO result = departmentInfoService.getDepartmentInfoList();
            if (result == null) {
                return ResponseEntity.ok(Result.error(404, "未找到云核心网产品线部门信息"));
            }
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}

