package com.tiqs.service;

import com.tiqs.entity.Assignment;

import java.util.List;

public interface AssignmentService {
    Assignment create(Assignment a);

    List<Assignment> listByCourse(Long courseId);

    Assignment get(Long id);

    Assignment update(Assignment a);

    void delete(Long id);
}
