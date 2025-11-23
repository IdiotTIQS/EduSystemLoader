package com.tiqs.controller;

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
    public ClassController(ClassService classService){this.classService=classService;}

    @PostMapping
    public ClassEntity create(@RequestBody ClassEntity cls){log.info("Create class request name={}", cls.getName()); return classService.create(cls);}    
    @GetMapping("/{id}")
    public ClassEntity get(@PathVariable Long id){log.debug("Get class {}", id); return classService.get(id);}    
    @GetMapping("/teacher/{teacherId}")
    public List<ClassEntity> listByTeacher(@PathVariable Long teacherId){log.debug("List classes teacher={}", teacherId); return classService.listByTeacher(teacherId);}    
    @PutMapping("/{id}")
    public ClassEntity updateName(@PathVariable Long id,@RequestParam String name){log.info("Update class {} name to {}", id,name); return classService.updateName(id,name);}    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){log.warn("Delete class {}", id); classService.delete(id);}    
    @GetMapping("/code/{code}")
    public ClassEntity findByCode(@PathVariable String code){log.debug("Find class by code {}", code); return classService.findByCode(code);}    
}
