/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description CourseService
 */
package com.tiqs.service;

import com.tiqs.entity.Course;

import java.util.List;

public interface CourseService {
    Course create(Course c);

    List<Course> listByClass(Long classId);

    Course get(Long id);

    Course update(Long id, String title, String description);

    void delete(Long id);
}
