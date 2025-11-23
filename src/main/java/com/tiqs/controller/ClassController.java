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
    public ClassEntity create(@RequestBody ClassEntity cls){log.info("请求创建班级 name={}", cls.getName()); return classService.create(cls);}    
    @GetMapping("/{id}")
    public ClassEntity get(@PathVariable Long id){log.debug("获取班级详情 id={}", id); return classService.get(id);}    
    @GetMapping("/teacher/{teacherId}")
    public List<ClassEntity> listByTeacher(@PathVariable Long teacherId){log.debug("查询教师的班级列表 teacherId={}", teacherId); return classService.listByTeacher(teacherId);}    
    @PutMapping("/{id}")
    public ClassEntity updateName(@PathVariable Long id,@RequestParam String name){log.info("更新班级名称 id={} name={}", id,name); return classService.updateName(id,name);}    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){log.warn("删除班级 id={}", id); classService.delete(id);}    
    @GetMapping("/code/{code}")
    public ClassEntity findByCode(@PathVariable String code){log.debug("按邀请码查询班级 code={}", code); return classService.findByCode(code);}    
}
