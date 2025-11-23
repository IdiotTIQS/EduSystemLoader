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
    public Submission submit(@RequestBody Submission s){log.info("学生提交作业 assignmentId={} studentId={}", s.getAssignmentId(),s.getStudentId()); return submissionService.submit(s);}    
    @GetMapping
    public List<Submission> listByAssignment(@RequestParam Long assignmentId){log.debug("查询作业提交列表 assignmentId={}", assignmentId); return submissionService.listByAssignment(assignmentId);}    
    @GetMapping("/unique")
    public Submission findUnique(@RequestParam Long assignmentId,@RequestParam Long studentId){log.debug("查询学生提交 assignmentId={} studentId={}", assignmentId,studentId); return submissionService.findUnique(assignmentId,studentId);}    
    @PutMapping("/{id}")
    public Submission updateContent(@PathVariable Long id,@RequestBody Submission s){log.info("更新作业提交 id={}", id); return submissionService.updateContent(id,s.getFilePath(),s.getAnswerText());}
    @PutMapping("/{id}/grade")
    public Submission grade(@PathVariable Long id,@RequestParam Double score,@RequestParam(required=false) String feedback){log.info("教师评分 submissionId={} score={}", id,score); return submissionService.grade(id,score,feedback);}    
}
