package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.CoursePlanningInfoVO;
import com.huawei.aitransform.service.CoursePlanningInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI课程规划明细表控制器
 */
@RestController
@RequestMapping("/course-planning-info")
public class CoursePlanningInfoController {

    @Autowired
    private CoursePlanningInfoService coursePlanningInfoService;

    /**
     * 查询所有课程规划明细数据
     * @return 课程规划明细列表
     */
    @GetMapping("/list")
    public ResponseEntity<Result<List<CoursePlanningInfoVO>>> getAllCoursePlanningInfo() {
        try {
            List<CoursePlanningInfoVO> result = coursePlanningInfoService.getAllCoursePlanningInfo();
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}

