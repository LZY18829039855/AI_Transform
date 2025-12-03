package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.UserConfigPermissionResponseVO;
import com.huawei.aitransform.service.UserConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户权限配置控制器
 */
@RestController
@RequestMapping("/user-config")
public class UserConfigController {

    @Autowired
    private UserConfigService userConfigService;

    /**
     * 查询所有有效用户的权限，将用户分为admin和非admin两组
     * @return 包含admin和非admin工号列表的响应对象
     */
    @GetMapping("/permissions")
    public ResponseEntity<Result<UserConfigPermissionResponseVO>> getUserPermissions() {
        try {
            UserConfigPermissionResponseVO result = userConfigService.getUserPermissions();
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}

