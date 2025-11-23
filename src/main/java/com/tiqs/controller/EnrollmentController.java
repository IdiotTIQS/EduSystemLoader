package com.tiqs.controller;

import com.tiqs.entity.Enrollment;
import com.tiqs.service.EnrollmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;
    public EnrollmentController(EnrollmentService enrollmentService){this.enrollmentService=enrollmentService;}

    @PostMapping
    public Enrollment enroll(@RequestParam Long classId,@RequestParam Long studentId){log.info("Enroll student {} to class {}", studentId,classId); return enrollmentService.enroll(classId,studentId);}    
    @GetMapping("/class/{classId}")
    public List<Enrollment> list(@PathVariable Long classId){log.debug("List enrollments class {}", classId); return enrollmentService.listByClass(classId);}    
    @GetMapping("/unique")
    public Enrollment findUnique(@RequestParam Long classId,@RequestParam Long studentId){log.debug("Check enrollment class={} student={}", classId,studentId); return enrollmentService.findUnique(classId,studentId);}    
}
