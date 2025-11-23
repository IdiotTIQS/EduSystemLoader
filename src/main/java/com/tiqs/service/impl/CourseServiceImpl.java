package com.tiqs.service.impl;

import com.tiqs.entity.Course;
import com.tiqs.mapper.CourseMapper;
import com.tiqs.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {
    private final CourseMapper courseMapper;
    public CourseServiceImpl(CourseMapper courseMapper){this.courseMapper=courseMapper;}

    @Transactional
    public Course create(Course c){courseMapper.insert(c); return c;}
    public List<Course> listByClass(Long classId){return courseMapper.findByClass(classId);}    
    public Course get(Long id){return courseMapper.findById(id);}    
    @Transactional
    public Course update(Long id,String title,String description){Course c = courseMapper.findById(id); if(c==null) return null; c.setTitle(title); c.setDescription(description); courseMapper.update(c); return c;}
    @Transactional
    public void delete(Long id){courseMapper.delete(id);}    
}
