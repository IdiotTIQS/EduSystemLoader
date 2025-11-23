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
    public Enrollment enroll(@RequestParam Long classId,@RequestParam Long studentId){log.info("学生加入班级 studentId={} classId={}", studentId,classId); return enrollmentService.enroll(classId,studentId);}    
    @GetMapping("/class/{classId}")
    public List<Enrollment> list(@PathVariable Long classId){log.debug("查询班级成员列表 classId={}", classId); return enrollmentService.listByClass(classId);}    
    @GetMapping("/unique")
    public Enrollment findUnique(@RequestParam Long classId,@RequestParam Long studentId){log.debug("检查学生是否已加入 classId={} studentId={}", classId,studentId); return enrollmentService.findUnique(classId,studentId);}    
}
