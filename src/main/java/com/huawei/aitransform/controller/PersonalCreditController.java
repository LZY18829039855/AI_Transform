package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.PersonalCredit;
import com.huawei.aitransform.entity.UserAccountResponseVO;
import com.huawei.aitransform.service.PersonalCreditService;
import com.huawei.aitransform.service.UserConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 个人学分控制器
 */
@RestController
@RequestMapping("/api/personal-credit")
public class PersonalCreditController {

    @Autowired
    private PersonalCreditService personalCreditService;

    @Autowired
    private UserConfigService userConfigService;

    /**
     * 触发个人学分数据同步（供外部定时任务平台调用）
     * @return 同步结果
     */
    @PostMapping("/sync")
    public ResponseEntity<Result<String>> syncPersonalCredits() {
        try {
            personalCreditService.syncAllPersonalCredits();
            return ResponseEntity.ok(Result.success("同步成功", "Sync completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "同步失败：" + e.getMessage()));
        }
    }

    /**
     * 获取个人学分概览
     * @param request HTTP请求
     * @param accountCookie Cookie中的账号
     * @return 个人学分概览数据
     */
    @GetMapping("/overview")
    public ResponseEntity<Result<PersonalCredit>> getPersonalCreditOverview(
            HttpServletRequest request,
            @CookieValue(value = "account", required = false) String accountCookie,
            @RequestParam(value = "account", required = false) String accountQuery) {
        try {
            String empNum;
            // 显式指定 account（工号）：AI School 明细下钻，与该行 employeeId 一致
            if (accountQuery != null && !accountQuery.trim().isEmpty()) {
                empNum = accountQuery.trim();
            } else {
                UserAccountResponseVO accountInfo = userConfigService.resolveCurrentUser(request, accountCookie);
                if (accountInfo == null || accountInfo.getEmpNum() == null || accountInfo.getEmpNum().trim().isEmpty()) {
                    return ResponseEntity.ok(Result.error(401, "未获取到用户信息，请先登录"));
                }
                empNum = accountInfo.getEmpNum().trim();
            }
            
            // 查询数据
            PersonalCredit credit = personalCreditService.getPersonalCreditOverview(empNum);
            
            if (credit == null) {
                // 如果没有数据，可能需要返回默认空对象或者空数据，视前端需求。
                // 这里返回一个空对象，避免前端报错
                credit = new PersonalCredit();
                credit.setEmployeeNumber(empNum);
                // 其他字段为null或0
            }
            
            return ResponseEntity.ok(Result.success("success", credit));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}
