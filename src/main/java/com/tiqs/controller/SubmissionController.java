package com.tiqs.controller;

import com.tiqs.entity.Submission;
import com.tiqs.service.SubmissionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {
    private final SubmissionService submissionService;
    public SubmissionController(SubmissionService submissionService){this.submissionService=submissionService;}

    @PostMapping
    public Submission submit(@RequestBody Submission s){return submissionService.submit(s);}    
    @GetMapping
    public List<Submission> listByAssignment(@RequestParam Long assignmentId){return submissionService.listByAssignment(assignmentId);}    
    @GetMapping("/unique")
    public Submission findUnique(@RequestParam Long assignmentId,@RequestParam Long studentId){return submissionService.findUnique(assignmentId,studentId);}    
    @PutMapping("/{id}")
    public Submission updateContent(@PathVariable Long id,@RequestBody Submission s){return submissionService.updateContent(id,s.getFilePath(),s.getAnswerText());}
    @PutMapping("/{id}/grade")
    public Submission grade(@PathVariable Long id,@RequestParam Double score,@RequestParam(required=false) String feedback){return submissionService.grade(id,score,feedback);}    
}
