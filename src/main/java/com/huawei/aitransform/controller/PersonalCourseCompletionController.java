package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.PersonalCourseCompletionResponseVO;
import com.huawei.aitransform.entity.UserAccountResponseVO;
import com.huawei.aitransform.service.PersonalCourseCompletionService;
import com.huawei.aitransform.service.UserConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 个人课程完成情况控制器
 */
@RestController
@RequestMapping("/personal-course")
public class PersonalCourseCompletionController {

    @Autowired
    private PersonalCourseCompletionService personalCourseCompletionService;

    @Autowired
    private UserConfigService userConfigService;

    /**
     * 查询个人课程完成情况
     * @param request HTTP请求对象，用于获取cookie
     * @param accountCookie 从cookie中获取的工号（可选，如果cookie名称为account）
     * @return 个人课程完成情况
     */
    @GetMapping("/completion")
    public ResponseEntity<Result<PersonalCourseCompletionResponseVO>> getPersonalCourseCompletion(
            HttpServletRequest request,
            @CookieValue(value = "account", required = false) String accountCookie) {
        try {
            // 从cookie中获取用户工号信息
            UserAccountResponseVO accountInfo = userConfigService.getUserAccountFromCookie(request, accountCookie);
            
            // 如果未获取到用户信息，返回错误提示
            if (accountInfo == null || accountInfo.getEmpNum() == null || accountInfo.getEmpNum().trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "未获取到用户信息，请先登录"));
            }
            
            // 获取不带首字母的工号
            String empNum = accountInfo.getEmpNum().trim();
            
            // 查询个人课程完成情况
            PersonalCourseCompletionResponseVO result = personalCourseCompletionService.getPersonalCourseCompletion(empNum);
            
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}

