package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.EmployeePO;
import com.huawei.aitransform.entity.UserAccountResponseVO;
import com.huawei.aitransform.mapper.EmployeeMapper;
import com.huawei.aitransform.service.UserConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 个人任职/认证信息查询（基于 t_employee 表）
 */
@RestController
@RequestMapping("/employee")
public class EmployeePersonalCertQualifiedController {

    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 查询个人任职认证信息：account 入参优先，否则从 Cookie 解析工号
     *
     * @param request       HTTP请求对象
     * @param account       工号入参（可选）
     * @param accountCookie cookie: account（可选）
     */
    @GetMapping("/personal-cert-qualified")
    public ResponseEntity<Result<EmployeePO>> getPersonalCertQualifiedInfo(
            HttpServletRequest request,
            @RequestParam(value = "account", required = false) String account,
            @CookieValue(value = "account", required = false) String accountCookie) {
        try {
            String empNum = null;

            if (account != null && !account.trim().isEmpty()) {
                empNum = account.trim();
            } else {
                UserAccountResponseVO accountInfo = userConfigService.getUserAccountFromCookie(request, accountCookie);
                if (accountInfo != null && accountInfo.getEmpNum() != null && !accountInfo.getEmpNum().trim().isEmpty()) {
                    empNum = accountInfo.getEmpNum().trim();
                }
            }

            if (empNum == null || empNum.isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "未获取到用户信息，请先登录"));
            }

            EmployeePO employee = employeeMapper.getEmployeeByEmployeeNumber(empNum);
            if (employee == null) {
                return ResponseEntity.ok(Result.error(404, "未查询到该工号对应的员工信息"));
            }

            employee.setQualifiedCredit(calcQualifiedCredit(employee.getCompetenceRatingCn()));
            employee.setCertCredit(calcCertCredit(employee.getCertTitle()));

            return ResponseEntity.ok(Result.success("查询成功", employee));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    private Integer calcQualifiedCredit(String competenceRatingCn) {
        if (competenceRatingCn == null || competenceRatingCn.trim().isEmpty()) {
            return 0;
        }
        String v = competenceRatingCn.trim();
        if ("3级".equals(v)) {
            return 10;
        }
        if ("4级".equals(v) || "5级".equals(v) || "6级".equals(v) || "7级".equals(v) || "8级".equals(v)) {
            return 25;
        }
        return 0;
    }

    private Integer calcCertCredit(String certTitle) {
        if (certTitle == null || certTitle.trim().isEmpty()) {
            return 0;
        }
        String v = certTitle.trim();
        if (v.contains("专业级")) {
            return 15;
        }
        if (v.contains("工作级")) {
            return 10;
        }
        return 0;
    }
}

