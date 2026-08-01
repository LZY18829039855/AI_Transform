package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.CoursePlanningInfoVO;
import com.huawei.aitransform.service.CoursePlanningInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI课程规划明细表控制器。
 * 规划表查询：/list（含部门选课）；管理端：/manage/list + 增删改（不限制 canEditCredit）。
 */
@RestController
@RequestMapping("/course-planning-info")
public class CoursePlanningInfoController {

    @Autowired
    private CoursePlanningInfoService coursePlanningInfoService;

    /**
     * 查询所有课程规划明细数据（含 selectedDepts，供训战课程规划表 / 部门目标选课只读展示）
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

    /**
     * 管理端：查询全部课程主数据（不含部门选课聚合）
     */
    @GetMapping("/manage/list")
    public ResponseEntity<Result<List<CoursePlanningInfoVO>>> getAllCoursesForManage() {
        try {
            List<CoursePlanningInfoVO> result = coursePlanningInfoService.getAllCoursesForManage();
            return ResponseEntity.ok(Result.success("查询成功", result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 按主键查询
     */
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Result<CoursePlanningInfoVO>> getById(@PathVariable("id") Integer id) {
        try {
            CoursePlanningInfoVO row = coursePlanningInfoService.getById(id);
            if (row == null) {
                return ResponseEntity.ok(Result.error(404, "课程不存在"));
            }
            return ResponseEntity.ok(Result.success("查询成功", row));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 新增课程（白名单成员均可操作，不校验 canEditCredit）
     */
    @PostMapping
    public ResponseEntity<Result<CoursePlanningInfoVO>> create(@RequestBody CoursePlanningInfoVO body) {
        try {
            CoursePlanningInfoVO saved = coursePlanningInfoService.create(body);
            return ResponseEntity.ok(Result.success("新增成功", saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 更新课程
     */
    @PutMapping("/{id:\\d+}")
    public ResponseEntity<Result<CoursePlanningInfoVO>> update(
            @PathVariable("id") Integer id,
            @RequestBody CoursePlanningInfoVO body) {
        try {
            CoursePlanningInfoVO updated = coursePlanningInfoService.update(id, body);
            if (updated == null) {
                return ResponseEntity.ok(Result.error(404, "课程不存在"));
            }
            return ResponseEntity.ok(Result.success("更新成功", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Result<Boolean>> delete(@PathVariable("id") Integer id) {
        try {
            boolean ok = coursePlanningInfoService.delete(id);
            if (!ok) {
                return ResponseEntity.ok(Result.error(404, "课程不存在或已删除"));
            }
            return ResponseEntity.ok(Result.success("删除成功", true));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "系统异常：" + e.getMessage()));
        }
    }
}
