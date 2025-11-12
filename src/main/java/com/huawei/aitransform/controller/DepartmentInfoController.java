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

    @Autowired
    private com.huawei.aitransform.mapper.DepartmentInfoMapper departmentInfoMapper;

    /**
     * 查询云核心网产品线的一级部门及其所有下属部门信息（树形结构）
     * 只返回云核心网产品线这一个2层部门及其下属部门，不包含其他2层部门
     * @return 部门信息树形结构（一级部门为根节点）
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

    /**
     * 测试接口：根据部门编码查询部门信息
     * @param deptCode 部门编码
     * @return 部门信息
     */
    @GetMapping("/by-code")
    public ResponseEntity<Result<DepartmentInfoVO>> getDepartmentByCode(
            @org.springframework.web.bind.annotation.RequestParam(value = "deptCode", required = true) String deptCode) {
        try {
            DepartmentInfoVO result = departmentInfoMapper.getDepartmentByCode(deptCode);
            if (result == null) {
                return ResponseEntity.ok(Result.error(404, "未找到部门信息：" + deptCode));
            }
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}

