package com.tiqs.controller;

import com.tiqs.auth.RequireRole;
import com.tiqs.auth.UserRole;
import com.tiqs.common.ApiResponse;
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

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @RequireRole(UserRole.STUDENT)
    @PostMapping
    public ApiResponse<Enrollment> enroll(@RequestParam Long classId, @RequestParam Long studentId) {
        log.info("学生加入班级 studentId={} classId={}", studentId, classId);
        return ApiResponse.ok(enrollmentService.enroll(classId, studentId));
    }

    @GetMapping("/class/{classId}")
    public ApiResponse<List<Enrollment>> list(@PathVariable Long classId) {
        log.debug("查询班级成员列表 classId={}", classId);
        return ApiResponse.ok(enrollmentService.listByClass(classId));
    }

    @GetMapping("/unique")
    public ApiResponse<Enrollment> findUnique(@RequestParam Long classId, @RequestParam Long studentId) {
        log.debug("检查学生是否已加入 classId={} studentId={}", classId, studentId);
        return ApiResponse.ok(enrollmentService.findUnique(classId, studentId));
    }
}
