/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description SubmissionController
 */
package com.tiqs.controller;

import com.tiqs.auth.RequireRole;
import com.tiqs.auth.UserRole;
import com.tiqs.common.ApiResponse;
import com.tiqs.entity.Submission;
import com.tiqs.service.SubmissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {
    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @RequireRole(UserRole.STUDENT)
    @PostMapping
    public ApiResponse<Submission> submit(@RequestBody Submission s) {
        log.info("学生提交作业 assignmentId={} studentId={}", s.getAssignmentId(), s.getStudentId());
        return ApiResponse.ok(submissionService.submit(s));
    }

    @RequireRole(UserRole.TEACHER)
    @GetMapping
    public ApiResponse<List<Submission>> listByAssignment(@RequestParam Long assignmentId) {
        log.debug("查询作业提交列表 assignmentId={}", assignmentId);
        return ApiResponse.ok(submissionService.listByAssignment(assignmentId));
    }

    @GetMapping("/unique")
    public ApiResponse<Submission> findUnique(@RequestParam Long assignmentId, @RequestParam Long studentId) {
        log.debug("查询学生提交 assignmentId={} studentId={}", assignmentId, studentId);
        return ApiResponse.ok(submissionService.findUnique(assignmentId, studentId));
    }

    @RequireRole(UserRole.STUDENT)
    @PutMapping("/{id}")
    public ApiResponse<Submission> updateContent(@PathVariable Long id, @RequestBody Submission s) {
        log.info("更新作业提交 id={}", id);
        return ApiResponse.ok(submissionService.updateContent(id, s.getFilePath(), s.getAnswerText(), s.getOriginalFileName()));
    }

    @RequireRole(UserRole.TEACHER)
    @PutMapping("/{id}/grade")
    public ApiResponse<Submission> grade(@PathVariable Long id, @RequestParam Double score, @RequestParam(required = false) String feedback) {
        log.info("教师评分 submissionId={} score={}", id, score);
        return ApiResponse.ok(submissionService.grade(id, score, feedback));
    }
}
