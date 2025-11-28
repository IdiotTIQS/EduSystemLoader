package com.tiqs.service.impl;

import com.tiqs.entity.Course;
import com.tiqs.mapper.CourseMapper;
import com.tiqs.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {
    private final CourseMapper courseMapper;

    public CourseServiceImpl(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    @Transactional
    public Course create(Course c) {
        courseMapper.insert(c);
        log.info("创建课程成功 id={} 标题={}", c.getId(), c.getTitle());
        return c;
    }

    public List<Course> listByClass(Long classId) {
        return courseMapper.findByClass(classId);
    }

    public Course get(Long id) {
        return courseMapper.findById(id);
    }

    @Transactional
    public Course update(Long id, String title, String description) {
        Course c = courseMapper.findById(id);
        if (c == null) {
            log.warn("课程不存在，更新失败 id={}", id);
            return null;
        }
        c.setTitle(title);
        c.setDescription(description);
        courseMapper.update(c);
        log.info("更新课程成功 id={}", id);
        return c;
    }

    @Transactional
    public void delete(Long id) {
        courseMapper.delete(id);
        log.info("删除课程成功 id={}", id);
    }
}
