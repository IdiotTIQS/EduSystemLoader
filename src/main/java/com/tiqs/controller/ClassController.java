package com.tiqs.controller;

import com.tiqs.entity.ClassEntity;
import com.tiqs.service.ClassService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class ClassController {
    private final ClassService classService;
    public ClassController(ClassService classService){this.classService=classService;}

    @PostMapping
    public ClassEntity create(@RequestBody ClassEntity cls){return classService.create(cls);}    
    @GetMapping("/{id}")
    public ClassEntity get(@PathVariable Long id){return classService.get(id);}    
    @GetMapping("/teacher/{teacherId}")
    public List<ClassEntity> listByTeacher(@PathVariable Long teacherId){return classService.listByTeacher(teacherId);}    
    @PutMapping("/{id}")
    public ClassEntity updateName(@PathVariable Long id,@RequestParam String name){return classService.updateName(id,name);}    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){classService.delete(id);}    
    @GetMapping("/code/{code}")
    public ClassEntity findByCode(@PathVariable String code){return classService.findByCode(code);}    
}
