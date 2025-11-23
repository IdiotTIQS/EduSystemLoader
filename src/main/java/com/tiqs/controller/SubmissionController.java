package com.tiqs.controller;

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
    public SubmissionController(SubmissionService submissionService){this.submissionService=submissionService;}

    @PostMapping
    public com.tiqs.handler.ApiResponse<Submission> submit(@RequestBody Submission s){log.info("学生提交作业 assignmentId={} studentId={}", s.getAssignmentId(),s.getStudentId()); return com.tiqs.handler.ApiResponse.ok(submissionService.submit(s));}    
    @GetMapping
    public com.tiqs.handler.ApiResponse<List<Submission>> listByAssignment(@RequestParam Long assignmentId){log.debug("查询作业提交列表 assignmentId={}", assignmentId); return com.tiqs.handler.ApiResponse.ok(submissionService.listByAssignment(assignmentId));}    
    @GetMapping("/unique")
    public com.tiqs.handler.ApiResponse<Submission> findUnique(@RequestParam Long assignmentId,@RequestParam Long studentId){log.debug("查询学生提交 assignmentId={} studentId={}", assignmentId,studentId); return com.tiqs.handler.ApiResponse.ok(submissionService.findUnique(assignmentId,studentId));}    
    @PutMapping("/{id}")
    public com.tiqs.handler.ApiResponse<Submission> updateContent(@PathVariable Long id,@RequestBody Submission s){log.info("更新作业提交 id={}", id); return com.tiqs.handler.ApiResponse.ok(submissionService.updateContent(id,s.getFilePath(),s.getAnswerText()));}
    @PutMapping("/{id}/grade")
    public com.tiqs.handler.ApiResponse<Submission> grade(@PathVariable Long id,@RequestParam Double score,@RequestParam(required=false) String feedback){log.info("教师评分 submissionId={} score={}", id,score); return com.tiqs.handler.ApiResponse.ok(submissionService.grade(id,score,feedback));}    
}
