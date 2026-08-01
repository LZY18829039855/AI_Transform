package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import com.huawei.aitransform.entity.DeptCourseSelection;
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
 * 部门目标选课（dept_course_selections）查询与增删改。
 */
@RestController
@RequestMapping("/dept-course-selections")
public class DeptCourseSelectionController {

    @Autowired
    private CoursePlanningInfoService coursePlanningInfoService;

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
    public ResponseEntity<Result<DeptCourseSelection>> create(@RequestBody DeptCourseSelection body) {
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
            @PathVariable("deptCode") String deptCode,
            @RequestBody DeptCourseSelection body) {
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
    public ResponseEntity<Result<Boolean>> delete(@PathVariable("deptCode") String deptCode) {
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
