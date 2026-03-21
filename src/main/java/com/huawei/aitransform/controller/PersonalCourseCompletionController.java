package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.DepartmentCourseCompletionRateVO;
import com.huawei.aitransform.entity.DepartmentEmployeeTrainingOverviewVO;
import com.huawei.aitransform.entity.PersonalCourseCompletionResponseVO;
import com.huawei.aitransform.entity.UserAccountResponseVO;
import com.huawei.aitransform.service.DepartmentCourseCompletionRateService;
import com.huawei.aitransform.service.DepartmentEmployeeTrainingOverviewService;
import com.huawei.aitransform.service.PersonalCourseCompletionService;
import com.huawei.aitransform.service.UserConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 个人课程完成情况控制器
 */
@RestController
@RequestMapping("/personal-course")
public class PersonalCourseCompletionController {

    private static final Set<String> AI_MATURITY_VALUES = new HashSet<>(Arrays.asList("L1", "L2", "L3"));

    @Autowired
    private PersonalCourseCompletionService personalCourseCompletionService;

    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private DepartmentCourseCompletionRateService departmentCourseCompletionRateService;

    @Autowired
    private DepartmentEmployeeTrainingOverviewService departmentEmployeeTrainingOverviewService;

    /**
     * 查询个人课程完成情况
     * @param request HTTP请求对象，用于获取cookie
     * @param account 工号入参（可选），不为空时优先使用；为空时从cookie获取
     * @param accountCookie 从cookie中获取的工号（可选，如果cookie名称为account）
     * @return 个人课程完成情况
     */
    @GetMapping("/completion")
    public ResponseEntity<Result<PersonalCourseCompletionResponseVO>> getPersonalCourseCompletion(
            HttpServletRequest request,
            @RequestParam(value = "account", required = false) String account,
            @CookieValue(value = "account", required = false) String accountCookie) {
        try {
            String empNum = null;
            // 工号入参不为空时优先使用入参
            if (account != null && !account.trim().isEmpty()) {
                empNum = account.trim();
            } else {
                // 否则复用原工号获取逻辑：从cookie中获取用户工号信息
                UserAccountResponseVO accountInfo = userConfigService.getUserAccountFromCookie(request, accountCookie);
                if (accountInfo != null && accountInfo.getEmpNum() != null && !accountInfo.getEmpNum().trim().isEmpty()) {
                    empNum = accountInfo.getEmpNum().trim();
                }
            }
            // 如果仍未获取到工号，返回错误提示
            if (empNum == null || empNum.isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "未获取到用户信息，请先登录"));
            }
            // 查询个人课程完成情况
            PersonalCourseCompletionResponseVO result = personalCourseCompletionService.getPersonalCourseCompletion(empNum);
            
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 部门课程完成率查询：根据父部门ID返回下一层级各部门的课程完成率统计
     * @param deptId    父部门ID（0 或云核心网二级部门时仅返回白名单内四级部门；三级返回四级子部门；四/五/六级返回下一层级子部门）
     * @param personType 人员类型：0 全员；1 干部；2 专家（与证书统计等接口约定一致）
     * @return 各部门统计列表
     */
    @GetMapping("/department-completion-rate")
    public ResponseEntity<Result<List<DepartmentCourseCompletionRateVO>>> getDepartmentCourseCompletionRate(
            @RequestParam(value = "deptId", required = true) String deptId,
            @RequestParam(value = "personType", required = false) Integer personType) {
        try {
            if (deptId == null || deptId.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "部门ID不能为空"));
            }
            Integer pt = (personType != null) ? personType : 0;
            if (pt != 0 && pt != 1 && pt != 2) {
                return ResponseEntity.ok(Result.error(400, "不支持的人员类型，目前仅支持全员（personType=0）、干部（personType=1）、专家（personType=2）"));
            }
            List<DepartmentCourseCompletionRateVO> list = departmentCourseCompletionRateService.getDepartmentCourseCompletionRate(deptId.trim(), pt);
            return ResponseEntity.ok(Result.success("查询成功", list));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 部门全员训战总览（下钻）：根据部门ID返回该部门下全员训战明细（含基础/进阶/实战及合计）
     * @param deptId    部门ID（部门编码）
     * @param personType 人员类型：0 全员；1 干部；2 专家
     * @param aiMaturity 岗位 AI 成熟度（可选）：L1、L2、L3；与 personType 配合——干部按 cadre_position_ai_maturity 过滤，专家按 expert_position_ai_maturity 过滤
     * @return 该部门下每名员工的训战总览列表
     */
    @GetMapping("/department-employee-training-overview")
    public ResponseEntity<Result<List<DepartmentEmployeeTrainingOverviewVO>>> getDepartmentEmployeeTrainingOverview(
            @RequestParam(value = "deptId", required = true) String deptId,
            @RequestParam(value = "personType", required = false) Integer personType,
            @RequestParam(value = "ai_maturity", required = false) String aiMaturity) {
        try {
            if (deptId == null || deptId.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "部门ID不能为空"));
            }
            Integer pt = (personType != null) ? personType : 0;
            if (pt != 0 && pt != 1 && pt != 2) {
                return ResponseEntity.ok(Result.error(400, "不支持的人员类型，目前仅支持全员（personType=0）、干部（personType=1）、专家（personType=2）"));
            }
            String maturityFilter = null;
            if (aiMaturity != null && !aiMaturity.trim().isEmpty()) {
                String normalized = aiMaturity.trim().toUpperCase();
                if (!AI_MATURITY_VALUES.contains(normalized)) {
                    return ResponseEntity.ok(Result.error(400, "ai_maturity 仅支持 L1、L2、L3"));
                }
                if (pt != 1 && pt != 2) {
                    return ResponseEntity.ok(Result.error(400, "岗位成熟度筛选仅在使用干部（personType=1）或专家（personType=2）时可用"));
                }
                maturityFilter = normalized;
            }
            List<DepartmentEmployeeTrainingOverviewVO> list = departmentEmployeeTrainingOverviewService.getDepartmentEmployeeTrainingOverview(deptId.trim(), pt, maturityFilter);
            return ResponseEntity.ok(Result.success("查询成功", list));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}

