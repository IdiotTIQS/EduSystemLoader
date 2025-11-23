package com.tiqs.controller;

import com.tiqs.entity.Course;
import com.tiqs.service.CourseService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService courseService;
    public CourseController(CourseService courseService){this.courseService=courseService;}

    @PostMapping
    public Course create(@RequestBody Course c){return courseService.create(c);}    
    @GetMapping("/{id}")
    public Course get(@PathVariable Long id){return courseService.get(id);}    
    @GetMapping
    public List<Course> listByClass(@RequestParam Long classId){return courseService.listByClass(classId);}    
    @PutMapping("/{id}")
    public Course update(@PathVariable Long id,@RequestBody Course c){return courseService.update(id,c.getTitle(),c.getDescription());}    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){courseService.delete(id);}    
}
