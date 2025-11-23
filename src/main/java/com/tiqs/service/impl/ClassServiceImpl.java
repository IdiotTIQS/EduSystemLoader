package com.tiqs.service.impl;

import com.tiqs.entity.ClassEntity;
import com.tiqs.mapper.ClassMapper;
import com.tiqs.service.ClassService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ClassServiceImpl implements ClassService {
    private final ClassMapper classMapper;
    public ClassServiceImpl(ClassMapper classMapper){this.classMapper=classMapper;}

    @Transactional
    public ClassEntity create(ClassEntity cls){
        classMapper.insert(cls);return cls;
    }
    public List<ClassEntity> listByTeacher(Long teacherId){return classMapper.findByTeacher(teacherId);}    
    public ClassEntity get(Long id){return classMapper.findById(id);}    
    @Transactional
    public ClassEntity updateName(Long id,String name){
        ClassEntity c = classMapper.findById(id); if(c==null) return null; c.setName(name); classMapper.updateName(c); return c;}
    @Transactional
    public void delete(Long id){classMapper.delete(id);}    
    public ClassEntity findByCode(String code){return classMapper.findByCode(code);}    
}
