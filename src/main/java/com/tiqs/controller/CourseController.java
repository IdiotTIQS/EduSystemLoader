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
    public Course create(@RequestBody Course c){log.info("Create course title={}", c.getTitle()); return courseService.create(c);}    
    @GetMapping("/{id}")
    public Course get(@PathVariable Long id){log.debug("Get course {}", id); return courseService.get(id);}    
    @GetMapping
    public List<Course> listByClass(@RequestParam Long classId){log.debug("List courses class={}", classId); return courseService.listByClass(classId);}    
    @PutMapping("/{id}")
    public Course update(@PathVariable Long id,@RequestBody Course c){log.info("Update course {}", id); return courseService.update(id,c.getTitle(),c.getDescription());}    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){log.warn("Delete course {}", id); courseService.delete(id);}    
}
