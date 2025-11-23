package com.tiqs.controller;

import com.tiqs.entity.Course;
import com.tiqs.service.CourseService;
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
    public Course create(@RequestBody Course c){log.info("请求创建课程 title={}", c.getTitle()); return courseService.create(c);}    
    @GetMapping("/{id}")
    public Course get(@PathVariable Long id){log.debug("获取课程详情 id={}", id); return courseService.get(id);}    
    @GetMapping
    public List<Course> listByClass(@RequestParam Long classId){log.debug("查询班级下课程列表 classId={}", classId); return courseService.listByClass(classId);}    
    @PutMapping("/{id}")
    public Course update(@PathVariable Long id,@RequestBody Course c){log.info("更新课程 id={}", id); return courseService.update(id,c.getTitle(),c.getDescription());}    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){log.warn("删除课程 id={}", id); courseService.delete(id);}    
}
