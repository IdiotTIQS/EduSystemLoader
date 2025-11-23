package com.tiqs.controller;

import com.tiqs.entity.Assignment;
import com.tiqs.service.AssignmentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;
    public AssignmentController(AssignmentService assignmentService){this.assignmentService=assignmentService;}

    @PostMapping
    public Assignment create(@RequestBody Assignment a){return assignmentService.create(a);}    
    @GetMapping("/{id}")
    public Assignment get(@PathVariable Long id){return assignmentService.get(id);}    
    @GetMapping
    public List<Assignment> listByCourse(@RequestParam Long courseId){return assignmentService.listByCourse(courseId);}    
    @PutMapping("/{id}")
    public Assignment update(@PathVariable Long id,@RequestBody Assignment a){a.setId(id);return assignmentService.update(a);}    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){assignmentService.delete(id);}    
}
