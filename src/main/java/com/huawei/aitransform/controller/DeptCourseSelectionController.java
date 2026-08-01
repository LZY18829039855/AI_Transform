package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.DeptCourseSelection;
import com.huawei.aitransform.service.CoursePlanningInfoService;
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
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 部门目标选课（dept_course_selections）查询与增删改。
 * 写操作需 canEditCredit，与多元化学分管理一致。
 */
@RestController
@RequestMapping("/dept-course-selections")
public class DeptCourseSelectionController {

    @Autowired
    private CoursePlanningInfoService coursePlanningInfoService;

    @Autowired
    private AccountModifierResolver accountModifierResolver;

    @Autowired
    private UserConfigService userConfigService;

    private boolean canEditCredit(String account) {
        return StringUtils.hasText(account)
                && userConfigService.getUserPermissionStatus(account).isCanEditCredit();
    }

    @GetMapping("/list")
    public ResponseEntity<Result<List<DeptCourseSelection>>> list() {
        try {
            return ResponseEntity.ok(Result.success("查询成功", coursePlanningInfoService.listDeptSelections()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    @GetMapping("/{deptCode}")
    public ResponseEntity<Result<DeptCourseSelection>> getByDeptCode(@PathVariable("deptCode") String deptCode) {
        try {
            DeptCourseSelection row = coursePlanningInfoService.getDeptSelection(deptCode);
            if (row == null) {
                return ResponseEntity.ok(Result.error(404, "部门选课配置不存在"));
            }
            return ResponseEntity.ok(Result.success("查询成功", row));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<Result<DeptCourseSelection>> create(
            HttpServletRequest request,
            @CookieValue(value = "account", required = false) String accountCookie,
            @RequestBody DeptCourseSelection body) {
        String modifier = accountModifierResolver.resolveModifierNumber(request, accountCookie);
        if (!StringUtils.hasText(modifier)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "未登录或无法从 Cookie 解析 account"));
        }
        if (!canEditCredit(modifier)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(403, "暂无多元化学分更新权限"));
        }
        try {
            DeptCourseSelection saved = coursePlanningInfoService.createDeptSelection(body);
            return ResponseEntity.ok(Result.success("新增成功", saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    @PutMapping("/{deptCode}")
    public ResponseEntity<Result<DeptCourseSelection>> update(
            HttpServletRequest request,
            @CookieValue(value = "account", required = false) String accountCookie,
            @PathVariable("deptCode") String deptCode,
            @RequestBody DeptCourseSelection body) {
        String modifier = accountModifierResolver.resolveModifierNumber(request, accountCookie);
        if (!StringUtils.hasText(modifier)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "未登录或无法从 Cookie 解析 account"));
        }
        if (!canEditCredit(modifier)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(403, "暂无多元化学分更新权限"));
        }
        try {
            DeptCourseSelection updated = coursePlanningInfoService.updateDeptSelection(deptCode, body);
            if (updated == null) {
                return ResponseEntity.ok(Result.error(404, "部门选课配置不存在"));
            }
            return ResponseEntity.ok(Result.success("更新成功", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    @DeleteMapping("/{deptCode}")
    public ResponseEntity<Result<Boolean>> delete(
            HttpServletRequest request,
            @CookieValue(value = "account", required = false) String accountCookie,
            @PathVariable("deptCode") String deptCode) {
        String modifier = accountModifierResolver.resolveModifierNumber(request, accountCookie);
        if (!StringUtils.hasText(modifier)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "未登录或无法从 Cookie 解析 account"));
        }
        if (!canEditCredit(modifier)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(403, "暂无多元化学分更新权限"));
        }
        try {
            boolean ok = coursePlanningInfoService.deleteDeptSelection(deptCode);
            if (!ok) {
                return ResponseEntity.ok(Result.error(404, "部门选课配置不存在或已删除"));
            }
            return ResponseEntity.ok(Result.success("删除成功", true));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}
