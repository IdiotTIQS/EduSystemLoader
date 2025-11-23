package com.tiqs.service;

import com.tiqs.entity.Enrollment;
import java.util.List;

public interface EnrollmentService {
    Enrollment enroll(Long classId,Long studentId);
    List<Enrollment> listByClass(Long classId);
    Enrollment findUnique(Long classId,Long studentId);
}
