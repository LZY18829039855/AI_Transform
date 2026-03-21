package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.PositionAiMaturityCourseCompletionRateVO;
import com.huawei.aitransform.service.PositionAiMaturityTrainingStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 训战课程相关接口（专家/干部按岗位 AI 成熟度统计等）
 */
@RestController
@RequestMapping("/trainning-courses")
public class TrainingCoursesController {

    @Autowired
    private PositionAiMaturityTrainingStatsService positionAiMaturityTrainingStatsService;

    /**
     * 专家、干部训战查询：按岗位 AI 成熟度（L1/L2/L3…）汇总课程目标均值、平均完课人数与完课率
     *
     * @param deptId     部门编码（必填）
     * @param personType 人员类型：1 干部；2 专家（必填）
     */
    @GetMapping("/maturity-trainning-courses")
    public ResponseEntity<Result<List<PositionAiMaturityCourseCompletionRateVO>>> listPositionAiMaturityStats(
            @RequestParam(value = "deptId", required = true) String deptId,
            @RequestParam(value = "personType", required = true) Integer personType) {
        try {
            if (deptId == null || deptId.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "部门ID不能为空"));
            }
            if (personType == null || (personType != 1 && personType != 2)) {
                return ResponseEntity.ok(Result.error(400, "人员类型仅支持干部（personType=1）、专家（personType=2）"));
            }
            List<PositionAiMaturityCourseCompletionRateVO> list =
                    positionAiMaturityTrainingStatsService.listByDeptAndPersonType(deptId.trim(), personType);
            return ResponseEntity.ok(Result.success("查询成功", list));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}
