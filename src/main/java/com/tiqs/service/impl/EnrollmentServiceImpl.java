package com.tiqs.service.impl;

import com.tiqs.entity.Enrollment;
import com.tiqs.mapper.EnrollmentMapper;
import com.tiqs.service.EnrollmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {
    private final EnrollmentMapper enrollmentMapper;
    public EnrollmentServiceImpl(EnrollmentMapper enrollmentMapper){this.enrollmentMapper=enrollmentMapper;}

    @Transactional
    public Enrollment enroll(Long classId,Long studentId){
        Enrollment existing = enrollmentMapper.findUnique(classId,studentId);
        if(existing!=null) return existing;
        Enrollment e = new Enrollment(); e.setClassId(classId); e.setStudentId(studentId); enrollmentMapper.insert(e); return e;
    }
    public List<Enrollment> listByClass(Long classId){return enrollmentMapper.findByClass(classId);}    
    public Enrollment findUnique(Long classId,Long studentId){return enrollmentMapper.findUnique(classId,studentId);}    
}
