package com.tiqs.controller;

import com.tiqs.entity.Enrollment;
import com.tiqs.service.EnrollmentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;
    public EnrollmentController(EnrollmentService enrollmentService){this.enrollmentService=enrollmentService;}

    @PostMapping
    public Enrollment enroll(@RequestParam Long classId,@RequestParam Long studentId){return enrollmentService.enroll(classId,studentId);}    
    @GetMapping("/class/{classId}")
    public List<Enrollment> list(@PathVariable Long classId){return enrollmentService.listByClass(classId);}    
    @GetMapping("/unique")
    public Enrollment findUnique(@RequestParam Long classId,@RequestParam Long studentId){return enrollmentService.findUnique(classId,studentId);}    
}
