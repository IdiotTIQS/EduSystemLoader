/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description ClassServiceImpl
 */
package com.tiqs.service.impl;

import com.tiqs.common.BusinessException;
import com.tiqs.entity.ClassEntity;
import com.tiqs.mapper.ClassMapper;
import com.tiqs.service.ClassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ClassServiceImpl implements ClassService {
    private final ClassMapper classMapper;

    public ClassServiceImpl(ClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    @Transactional
    public ClassEntity create(ClassEntity cls) {
        classMapper.insert(cls);
        log.info("创建班级成功 id={} code={}", cls.getId(), cls.getCode());
        return cls;
    }

    public List<ClassEntity> listByTeacher(Long teacherId) {
        log.debug("按教师查询班级 teacherId={}", teacherId);
        return classMapper.findByTeacher(teacherId);
    }

    public ClassEntity get(Long id) {
        return classMapper.findById(id);
    }

    @Transactional
    public ClassEntity updateName(Long id, String name) {
        ClassEntity c = classMapper.findById(id);
        if (c == null) {
            log.warn("更新失败，班级不存在 id={}", id);
            return null;
        }
        c.setName(name);
        classMapper.updateName(c);
        log.info("更新班级名称成功 id={} name={}", id, name);
        return c;
    }

    @Transactional
    public void delete(Long id) {
        classMapper.delete(id);
        log.info("删除班级成功 id={}", id);
    }

    public ClassEntity findByCode(String code) {
        ClassEntity cls = classMapper.findByCode(code);
        if (cls == null) {
            log.warn("按邀请码查询班级未找到 code={}", code);
            throw BusinessException.of(1001, "班级邀请码不存在！请检查班级邀请码输入是否正确！");
        }
        return cls;
    }
}
