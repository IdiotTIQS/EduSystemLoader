/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description ClassService
 */
package com.tiqs.service;

import com.tiqs.entity.ClassEntity;

import java.util.List;

public interface ClassService {
    ClassEntity create(ClassEntity cls);

    List<ClassEntity> listByTeacher(Long teacherId);

    ClassEntity get(Long id);

    ClassEntity updateName(Long id, String name);

    void delete(Long id);

    ClassEntity findByCode(String code);
}
