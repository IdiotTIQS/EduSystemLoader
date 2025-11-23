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
    public com.tiqs.handler.ApiResponse<Enrollment> enroll(@RequestParam Long classId,@RequestParam Long studentId){log.info("学生加入班级 studentId={} classId={}", studentId,classId); return com.tiqs.handler.ApiResponse.ok(enrollmentService.enroll(classId,studentId));}    
    @GetMapping("/class/{classId}")
    public com.tiqs.handler.ApiResponse<List<Enrollment>> list(@PathVariable Long classId){log.debug("查询班级成员列表 classId={}", classId); return com.tiqs.handler.ApiResponse.ok(enrollmentService.listByClass(classId));}    
    @GetMapping("/unique")
    public com.tiqs.handler.ApiResponse<Enrollment> findUnique(@RequestParam Long classId,@RequestParam Long studentId){log.debug("检查学生是否已加入 classId={} studentId={}", classId,studentId); return com.tiqs.handler.ApiResponse.ok(enrollmentService.findUnique(classId,studentId));}    
}
