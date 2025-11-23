package com.tiqs.service.impl;

import com.tiqs.entity.ClassEntity;
import com.tiqs.mapper.ClassMapper;
import com.tiqs.service.ClassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
public class ClassServiceImpl implements ClassService {
    private final ClassMapper classMapper;
    public ClassServiceImpl(ClassMapper classMapper){this.classMapper=classMapper;}

    @Transactional
    public ClassEntity create(ClassEntity cls){
        classMapper.insert(cls); log.info("Created class id={} code={}", cls.getId(), cls.getCode()); return cls;
    }
    public List<ClassEntity> listByTeacher(Long teacherId){log.debug("List classes by teacher {}", teacherId); return classMapper.findByTeacher(teacherId);}    
    public ClassEntity get(Long id){return classMapper.findById(id);}    
    @Transactional
    public ClassEntity updateName(Long id,String name){
        ClassEntity c = classMapper.findById(id); if(c==null){log.warn("Class {} not found for update", id); return null;} c.setName(name); classMapper.updateName(c); log.info("Updated class {} name to {}", id, name); return c;}
    @Transactional
    public void delete(Long id){classMapper.delete(id); log.info("Deleted class {}", id);}    
    public ClassEntity findByCode(String code){return classMapper.findByCode(code);}    
}
