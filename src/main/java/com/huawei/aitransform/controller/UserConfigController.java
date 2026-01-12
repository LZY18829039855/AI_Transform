package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.UserAccountResponseVO;
import com.huawei.aitransform.service.UserConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户权限配置控制器
 */
@RestController
@RequestMapping("/user-config")
public class UserConfigController {

    @Autowired
    private UserConfigService userConfigService;

    /**
     * 查询cookie中的用户工号信息
     * @param request HTTP请求对象，用于获取cookie
     * @param accountCookie 从cookie中获取的工号（可选，如果cookie名称为account）
     * @return 包含emp_num和w3_account的用户工号信息，如果未获取到则返回null
     */
    @GetMapping("/account")
    public ResponseEntity<Result<UserAccountResponseVO>> getUserAccount(
            HttpServletRequest request,
            @CookieValue(value = "account", required = false) String accountCookie) {
        try {
            UserAccountResponseVO accountInfo = userConfigService.getUserAccountFromCookie(request, accountCookie);
            return ResponseEntity.ok(Result.success("查询成功", accountInfo));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 验证用户是否为有效用户
     * @param request HTTP请求对象，用于获取cookie
     * @param accountCookie 从cookie中获取的工号（可选，如果cookie名称为account）
     * @return true表示是有效用户，false表示不是有效用户或不存在
     */
    @GetMapping("/permissions")
    public ResponseEntity<Result<Boolean>> getUserPermissions(
            HttpServletRequest request,
            @CookieValue(value = "account", required = false) String accountCookie) {
        try {
            // 从cookie中获取用户工号信息
            UserAccountResponseVO accountInfo = userConfigService.getUserAccountFromCookie(request, accountCookie);
            
            // 如果未获取到工号，返回false
            if (accountInfo == null || accountInfo.getW3Account() == null || accountInfo.getW3Account().trim().isEmpty()) {
                return ResponseEntity.ok(Result.success("查询成功", false));
            }
            
            // 查询数据库中是否存在该有效用户（使用w3_account进行验证）
            boolean isValid = userConfigService.isValidUser(accountInfo.getW3Account());
            return ResponseEntity.ok(Result.success("查询成功", isValid));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}

