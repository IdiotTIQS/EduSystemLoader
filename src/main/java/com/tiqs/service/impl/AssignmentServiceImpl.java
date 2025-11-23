package com.tiqs.service.impl;

import com.tiqs.entity.Assignment;
import com.tiqs.mapper.AssignmentMapper;
import com.tiqs.service.AssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {
    private final AssignmentMapper assignmentMapper;
    public AssignmentServiceImpl(AssignmentMapper assignmentMapper){this.assignmentMapper=assignmentMapper;}

    @Transactional
    public Assignment create(Assignment a){assignmentMapper.insert(a); return a;}
    public List<Assignment> listByCourse(Long courseId){return assignmentMapper.findByCourse(courseId);}    
    public Assignment get(Long id){return assignmentMapper.findById(id);}    
    @Transactional
    public Assignment update(Assignment a){assignmentMapper.update(a); return assignmentMapper.findById(a.getId());}
    @Transactional
    public void delete(Long id){assignmentMapper.delete(id);}    
}
