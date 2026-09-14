package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.EmployeePO;
import com.huawei.aitransform.entity.Subject2ExamDetailVO;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 个人任职/认证信息查询（实时计算，口径与同步写入 t_employee 一致）
 */
@RestController
@RequestMapping("/employee")
public class EmployeePersonalCertQualifiedController {

    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 查询个人任职认证信息：account 入参优先，否则从 Cookie 解析工号。
     * 实时从 t_employee_sync + 任职/认证/考试表计算，不过滤职位族。
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

            EmployeePO employee = employeeMapper.getRealtimeEmployeeCertQualifiedByEmployeeNumber(empNum);
            if (employee == null) {
                return ResponseEntity.ok(Result.error(404, "未查询到该工号对应的员工信息"));
            }

            employee.setQualifiedCredit(calcQualifiedCredit(employee.getCompetenceRatingCn()));
            Integer certCredit = calcCertCredit(employee.getCertTitle());
            employee.setCertCredit(certCredit);
            employee.setSubject2List(buildSubject2List(empNum, certCredit));

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
        if ("2级".equals(v)) {
            return 5;
        }
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

    /**
     * 认证未通过时返回科目二明细；已有认证学分则不展示。
     * 多条通过记录时仅首条计 5 分，与学分同步口径一致。
     */
    private List<Subject2ExamDetailVO> buildSubject2List(String empNum, Integer certCredit) {
        if (certCredit != null && certCredit > 0) {
            return Collections.emptyList();
        }
        List<Subject2ExamDetailVO> rows = employeeMapper.getPassedSubject2ExamsByEmployeeNumber(empNum);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<Subject2ExamDetailVO> result = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            Subject2ExamDetailVO row = rows.get(i);
            if (row == null) {
                continue;
            }
            Subject2ExamDetailVO item = new Subject2ExamDetailVO();
            item.setExamName(row.getExamName());
            item.setCredit(i == 0 ? 5 : 0);
            result.add(item);
        }
        return result;
    }
}

