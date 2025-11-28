package com.tiqs.service.impl;

import com.tiqs.entity.Enrollment;
import com.tiqs.mapper.EnrollmentMapper;
import com.tiqs.service.EnrollmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {
    private final EnrollmentMapper enrollmentMapper;

    public EnrollmentServiceImpl(EnrollmentMapper enrollmentMapper) {
        this.enrollmentMapper = enrollmentMapper;
    }

    @Transactional
    public Enrollment enroll(Long classId, Long studentId) {
        Enrollment existing = enrollmentMapper.findUnique(classId, studentId);
        if (existing != null) {
            log.info("学生已加入班级 studentId={} classId={}", studentId, classId);
            return existing;
        }
        Enrollment e = new Enrollment();
        e.setClassId(classId);
        e.setStudentId(studentId);
        enrollmentMapper.insert(e);
        log.info("学生加入班级成功 studentId={} classId={}", studentId, classId);
        return e;
    }

    public List<Enrollment> listByClass(Long classId) {
        return enrollmentMapper.findByClass(classId);
    }

    public Enrollment findUnique(Long classId, Long studentId) {
        return enrollmentMapper.findUnique(classId, studentId);
    }
}
