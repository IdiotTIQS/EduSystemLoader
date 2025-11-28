package com.tiqs.service.impl;

import com.tiqs.entity.Assignment;
import com.tiqs.mapper.AssignmentMapper;
import com.tiqs.service.AssignmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AssignmentServiceImpl implements AssignmentService {
    private final AssignmentMapper assignmentMapper;

    public AssignmentServiceImpl(AssignmentMapper assignmentMapper) {
        this.assignmentMapper = assignmentMapper;
    }

    @Transactional
    public Assignment create(Assignment a) {
        assignmentMapper.insert(a);
        log.info("创建作业成功 id={} 课程={}", a.getId(), a.getCourseId());
        return a;
    }

    public List<Assignment> listByCourse(Long courseId) {
        return assignmentMapper.findByCourse(courseId);
    }

    public Assignment get(Long id) {
        return assignmentMapper.findById(id);
    }

    @Transactional
    public Assignment update(Assignment a) {
        assignmentMapper.update(a);
        log.info("更新作业成功 id={}", a.getId());
        return assignmentMapper.findById(a.getId());
    }

    @Transactional
    public void delete(Long id) {
        assignmentMapper.delete(id);
        log.info("删除作业成功 id={}", id);
    }
}
