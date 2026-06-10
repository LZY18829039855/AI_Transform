package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.UserAccountResponseVO;
import com.huawei.aitransform.entity.UserPermissionStatusVO;
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
     * 验证用户是否为有效用户（工号取自 request 中的 account cookie；若首字符为英文字母，校验前会去掉该首字符）
     * @param request HTTP请求对象，用于获取cookie
     * @param accountCookie 从cookie中获取的工号（可选，如果cookie名称为account）
     * @return member 表示是否在白名单，asAdmin 表示是否为管理员
     */
    @GetMapping("/permissions")
    public ResponseEntity<Result<UserPermissionStatusVO>> getUserPermissions(
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

            // 如果未获取到工号，返回无权限状态
            if (account == null || account.trim().isEmpty()) {
                return ResponseEntity.ok(Result.success("查询成功", new UserPermissionStatusVO(false, false)));
            }
            UserPermissionStatusVO status = userConfigService.getUserPermissionStatus(account);
            return ResponseEntity.ok(Result.success("查询成功", status));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}

