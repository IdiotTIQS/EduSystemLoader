package com.tiqs.controller;

import com.tiqs.auth.RequireRole;
import com.tiqs.auth.UserRole;
import com.tiqs.common.ApiResponse;
import com.tiqs.entity.ClassEntity;
import com.tiqs.service.ClassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/classes")
public class ClassController {
    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @RequireRole(UserRole.TEACHER)
    @PostMapping
    public ApiResponse<ClassEntity> create(@RequestBody ClassEntity cls) {
        log.info("请求创建班级 name={}", cls.getName());
        return ApiResponse.ok(classService.create(cls));
    }

    @GetMapping("/{id}")
    public ApiResponse<ClassEntity> get(@PathVariable Long id) {
        log.debug("获取班级详情 id={}", id);
        return ApiResponse.ok(classService.get(id));
    }

    @RequireRole(UserRole.TEACHER)
    @GetMapping("/teacher/{teacherId}")
    public ApiResponse<List<ClassEntity>> listByTeacher(@PathVariable Long teacherId) {
        log.debug("查询教师的班级列表 teacherId={}", teacherId);
        return ApiResponse.ok(classService.listByTeacher(teacherId));
    }

    @RequireRole(UserRole.TEACHER)
    @PutMapping("/{id}")
    public ApiResponse<ClassEntity> updateName(@PathVariable Long id, @RequestParam String name) {
        log.info("更新班级名称 id={} name={}", id, name);
        return ApiResponse.ok(classService.updateName(id, name));
    }

    @RequireRole(UserRole.TEACHER)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        log.warn("删除班级 id={}", id);
        classService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/code/{code}")
    public ApiResponse<ClassEntity> findByCode(@PathVariable String code) {
        log.debug("按邀请码查询班级 code={}", code);
        return ApiResponse.ok(classService.findByCode(code));
    }
}
