package com.tiqs.controller;

import com.tiqs.entity.Course;
import com.tiqs.service.CourseService;
import com.tiqs.common.ApiResponse; // updated import
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService courseService;
    public CourseController(CourseService courseService){this.courseService=courseService;}

    @PostMapping
    public ApiResponse<Course> create(@RequestBody Course c){log.info("请求创建课程 title={}", c.getTitle()); return ApiResponse.ok(courseService.create(c));}
    @GetMapping("/{id}")
    public ApiResponse<Course> get(@PathVariable Long id){log.debug("获取课程详情 id={}", id); return ApiResponse.ok(courseService.get(id));}
    @GetMapping
    public ApiResponse<List<Course>> listByClass(@RequestParam Long classId){log.debug("查询班级下课程列表 classId={}", classId); return ApiResponse.ok(courseService.listByClass(classId));}
    @PutMapping("/{id}")
    public ApiResponse<Course> update(@PathVariable Long id,@RequestBody Course c){log.info("更新课程 id={}", id); return ApiResponse.ok(courseService.update(id,c.getTitle(),c.getDescription()));}
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id){log.warn("删除课程 id={}", id); courseService.delete(id); return ApiResponse.ok(null);}
}
