/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description AssignmentController
 */
package com.tiqs.controller;

import com.tiqs.auth.RequireRole;
import com.tiqs.auth.UserRole;
import com.tiqs.common.ApiResponse;
import com.tiqs.entity.Assignment;
import com.tiqs.service.AssignmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @RequireRole(UserRole.TEACHER)
    @PostMapping
    public ApiResponse<Assignment> create(@RequestBody Assignment a) {
        log.info("请求创建作业 courseId={} title={}", a.getCourseId(), a.getTitle());
        return ApiResponse.ok(assignmentService.create(a));
    }

    @GetMapping("/{id}")
    public ApiResponse<Assignment> get(@PathVariable Long id) {
        log.debug("获取作业详情 id={}", id);
        return ApiResponse.ok(assignmentService.get(id));
    }

    @GetMapping
    public ApiResponse<List<Assignment>> listByCourse(@RequestParam Long courseId) {
        log.debug("查询课程下作业列表 courseId={}", courseId);
        return ApiResponse.ok(assignmentService.listByCourse(courseId));
    }

    @RequireRole(UserRole.TEACHER)
    @PutMapping("/{id}")
    public ApiResponse<Assignment> update(@PathVariable Long id, @RequestBody Assignment a) {
        a.setId(id);
        log.info("更新作业 id={}", id);
        return ApiResponse.ok(assignmentService.update(a));
    }

    @RequireRole(UserRole.TEACHER)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        log.warn("删除作业 id={}", id);
        assignmentService.delete(id);
        return ApiResponse.ok(null);
    }
}
