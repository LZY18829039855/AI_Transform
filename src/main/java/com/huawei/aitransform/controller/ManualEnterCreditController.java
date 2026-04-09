package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.PageResult;
import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.ManualEnterCredit;
import com.huawei.aitransform.entity.ManualEnterCreditBatchImportRequest;
import com.huawei.aitransform.entity.ManualEnterCreditBatchImportResult;
import com.huawei.aitransform.service.ManualEnterCreditService;
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

import javax.servlet.http.HttpServletRequest;

/**
 * 手动录入学分（t_manual_enter_credit）增删改查与批量导入。
 * 新增、更新、批量导入时从 Cookie account 解析操作人，写入 modifierNumber（库列 Modifier__number）。
 */
@RestController
@RequestMapping("/manual-enter-credit")
public class ManualEnterCreditController {

    @Autowired
    private ManualEnterCreditService manualEnterCreditService;

    @Autowired
    private AccountModifierResolver accountModifierResolver;

    /**
     * 分页查询
     */
    @GetMapping("/list")
    public ResponseEntity<Result<PageResult<ManualEnterCredit>>> list(
            @RequestParam(value = "employeeNumber", required = false) String employeeNumber,
            @RequestParam(value = "employeeName", required = false) String employeeName,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        try {
            PageResult<ManualEnterCredit> data =
                    manualEnterCreditService.page(employeeNumber, employeeName, pageNum, pageSize);
            return ResponseEntity.ok(Result.success("查询成功", data));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 按主键查询
     */
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Result<ManualEnterCredit>> getById(@PathVariable("id") Integer id) {
        try {
            ManualEnterCredit row = manualEnterCreditService.getById(id);
            if (row == null) {
                return ResponseEntity.ok(Result.error(404, "记录不存在"));
            }
            return ResponseEntity.ok(Result.success("查询成功", row));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 新增（操作人由 Cookie account 解析，忽略请求体中的 modifierNumber）
     */
    @PostMapping
    public ResponseEntity<Result<ManualEnterCredit>> create(
            HttpServletRequest request,
            @CookieValue(value = "account", required = false) String accountCookie,
            @RequestBody ManualEnterCredit body) {
        String modifier = accountModifierResolver.resolveModifierNumber(request, accountCookie);
        if (!StringUtils.hasText(modifier)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "未登录或无法从 Cookie 解析 account，无法记录操作人"));
        }
        try {
            ManualEnterCredit saved = manualEnterCreditService.create(body, modifier);
            return ResponseEntity.ok(Result.success("新增成功", saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 全量更新（操作人由 Cookie account 解析并覆盖库中 Modifier__number）
     */
    @PutMapping("/{id:\\d+}")
    public ResponseEntity<Result<ManualEnterCredit>> update(
            HttpServletRequest request,
            @CookieValue(value = "account", required = false) String accountCookie,
            @PathVariable("id") Integer id,
            @RequestBody ManualEnterCredit body) {
        String modifier = accountModifierResolver.resolveModifierNumber(request, accountCookie);
        if (!StringUtils.hasText(modifier)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "未登录或无法从 Cookie 解析 account，无法记录操作人"));
        }
        try {
            ManualEnterCredit updated = manualEnterCreditService.update(id, body, modifier);
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
     * 批量导入：一次请求提交多行，服务端单条 SQL 批量插入（非多次调用单次新增接口）
     */
    @PostMapping("/batch-import")
    public ResponseEntity<Result<ManualEnterCreditBatchImportResult>> batchImport(
            HttpServletRequest request,
            @CookieValue(value = "account", required = false) String accountCookie,
            @RequestBody ManualEnterCreditBatchImportRequest body) {
        String modifier = accountModifierResolver.resolveModifierNumber(request, accountCookie);
        if (!StringUtils.hasText(modifier)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "未登录或无法从 Cookie 解析 account，无法记录操作人"));
        }
        try {
            ManualEnterCreditBatchImportResult result =
                    manualEnterCreditService.batchImport(body == null ? null : body.getRows(), modifier);
            return ResponseEntity.ok(Result.success("批量导入成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Result<Boolean>> delete(@PathVariable("id") Integer id) {
        try {
            boolean ok = manualEnterCreditService.delete(id);
            if (!ok) {
                return ResponseEntity.ok(Result.error(404, "记录不存在或已删除"));
            }
            return ResponseEntity.ok(Result.success("删除成功", true));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}
