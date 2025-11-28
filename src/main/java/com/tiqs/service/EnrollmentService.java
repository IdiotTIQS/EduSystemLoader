/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description EnrollmentService
 */
package com.tiqs.service;

import com.tiqs.entity.Enrollment;

import java.util.List;

public interface EnrollmentService {
    Enrollment enroll(Long classId, Long studentId);

    List<Enrollment> listByClass(Long classId);

    Enrollment findUnique(Long classId, Long studentId);
}
