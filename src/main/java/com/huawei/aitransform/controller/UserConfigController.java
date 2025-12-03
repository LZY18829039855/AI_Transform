package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.service.UserConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
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
            // 优先使用 @CookieValue 获取的 account cookie
            String account = accountCookie;
            
            // 如果 @CookieValue 没有获取到，尝试从 HttpServletRequest 中获取
            if (account == null || account.trim().isEmpty()) {
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        // 只判断cookie名称为account
                        if ("account".equals(cookie.getName())) {
                            account = cookie.getValue();
                            break;
                        }
                    }
                }
            }
            
            // 如果未获取到工号，返回false
            if (account == null || account.trim().isEmpty()) {
                return ResponseEntity.ok(Result.success("查询成功", false));
            }
            
            // 查询数据库中是否存在该有效用户
            boolean isValid = userConfigService.isValidUser(account);
            return ResponseEntity.ok(Result.success("查询成功", isValid));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}

