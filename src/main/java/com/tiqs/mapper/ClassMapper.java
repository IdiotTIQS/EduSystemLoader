package com.tiqs.mapper;

import com.tiqs.entity.ClassEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ClassMapper {
    @Insert("INSERT INTO classes(name, code, teacher_id, created_at) VALUES(#{name},#{code},#{teacherId},NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ClassEntity cls);

    @Select("SELECT id,name,code,teacher_id AS teacherId,created_at AS createdAt FROM classes WHERE id=#{id}")
    ClassEntity findById(Long id);

    @Select("SELECT id,name,code,teacher_id AS teacherId,created_at AS createdAt FROM classes WHERE teacher_id=#{teacherId}")
    List<ClassEntity> findByTeacher(Long teacherId);

    @Update("UPDATE classes SET name=#{name} WHERE id=#{id}")
    int updateName(ClassEntity cls);

    @Delete("DELETE FROM classes WHERE id=#{id}")
    int delete(Long id);

    @Select("SELECT id,name,code,teacher_id AS teacherId,created_at AS createdAt FROM classes WHERE code=#{code}")
    ClassEntity findByCode(String code);
}
