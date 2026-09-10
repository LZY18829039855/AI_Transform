package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.PageResult;
import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.UserAccountResponseVO;
import com.huawei.aitransform.entity.UserConfigManageVO;
import com.huawei.aitransform.entity.UserPermissionStatusVO;
import com.huawei.aitransform.service.UserConfigService;
import com.huawei.aitransform.util.AccountModifierResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Autowired
    private AccountModifierResolver accountModifierResolver;

    private boolean isAdmin(String account) {
        return StringUtils.hasText(account)
                && userConfigService.getUserPermissionStatus(account).isAsAdmin();
    }

    private boolean canEditCredit(String account) {
        return StringUtils.hasText(account)
                && userConfigService.getUserPermissionStatus(account).isCanEditCredit();
    }

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
     * 查询当前用户权限状态（工号取自 request 中的 account cookie；若首字符为英文字母，校验前会去掉该首字符）
     * @param request HTTP请求对象，用于获取cookie
     * @param accountCookie 从cookie中获取的工号（可选，如果cookie名称为account）
     * @return member 表示是否具备普通用户访问权限（全员开放：有工号即为 true），
     * asAdmin 表示是否为管理员（查 user_config），
     * canEditCredit 表示是否可更新多元化学分（查 user_config）
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

    /**
     * 权限管理分页列表：admin 可查看
     */
    @GetMapping("/list")
    public ResponseEntity<Result<PageResult<UserConfigManageVO>>> list(
            HttpServletRequest request,
            @CookieValue(value = "account", required = false) String accountCookie,
            @RequestParam(value = "filterAccount", required = false) String filterAccount,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        String modifier = accountModifierResolver.resolveModifierNumber(request, accountCookie);
        if (!StringUtils.hasText(modifier)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "未登录或无法从 Cookie 解析 account"));
        }
        if (!isAdmin(modifier)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(403, "暂无访问权限"));
        }
        try {
            PageResult<UserConfigManageVO> data = userConfigService.page(filterAccount, pageNum, pageSize);
            return ResponseEntity.ok(Result.success("查询成功", data));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 新增权限配置：超级用户（canEditCredit）可操作
     */
    @PostMapping
    public ResponseEntity<Result<UserConfigManageVO>> create(
            HttpServletRequest request,
            @CookieValue(value = "account", required = false) String accountCookie,
            @RequestBody UserConfigManageVO body) {
        String modifier = accountModifierResolver.resolveModifierNumber(request, accountCookie);
        if (!StringUtils.hasText(modifier)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "未登录或无法从 Cookie 解析 account"));
        }
        if (!canEditCredit(modifier)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(403, "暂无权限配置更新权限"));
        }
        try {
            UserConfigManageVO saved = userConfigService.create(body);
            return ResponseEntity.ok(Result.success("新增成功", saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 更新权限配置：超级用户可操作
     */
    @PutMapping("/{id:\\d+}")
    public ResponseEntity<Result<UserConfigManageVO>> update(
            HttpServletRequest request,
            @CookieValue(value = "account", required = false) String accountCookie,
            @PathVariable("id") Integer id,
            @RequestBody UserConfigManageVO body) {
        String modifier = accountModifierResolver.resolveModifierNumber(request, accountCookie);
        if (!StringUtils.hasText(modifier)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "未登录或无法从 Cookie 解析 account"));
        }
        if (!canEditCredit(modifier)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(403, "暂无权限配置更新权限"));
        }
        try {
            UserConfigManageVO updated = userConfigService.update(id, body, modifier);
            if (updated == null) {
                return ResponseEntity.ok(Result.error(404, "记录不存在"));
            }
            return ResponseEntity.ok(Result.success("更新成功", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 删除权限配置：超级用户可操作
     */
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Result<Boolean>> delete(
            HttpServletRequest request,
            @CookieValue(value = "account", required = false) String accountCookie,
            @PathVariable("id") Integer id) {
        String modifier = accountModifierResolver.resolveModifierNumber(request, accountCookie);
        if (!StringUtils.hasText(modifier)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "未登录或无法从 Cookie 解析 account"));
        }
        if (!canEditCredit(modifier)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(403, "暂无权限配置更新权限"));
        }
        try {
            boolean ok = userConfigService.delete(id, modifier);
            if (!ok) {
                return ResponseEntity.ok(Result.error(404, "记录不存在或已删除"));
            }
            return ResponseEntity.ok(Result.success("删除成功", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}
