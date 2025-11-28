package com.tiqs.mapper;

import com.tiqs.entity.Enrollment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EnrollmentMapper {
    @Insert("INSERT INTO enrollments(class_id, student_id, joined_at) VALUES(#{classId},#{studentId},NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Enrollment e);

    @Select("SELECT id,class_id AS classId,student_id AS studentId,joined_at AS joinedAt FROM enrollments WHERE class_id=#{classId} AND student_id=#{studentId}")
    Enrollment findUnique(@Param("classId") Long classId, @Param("studentId") Long studentId);

    @Select("SELECT id,class_id AS classId,student_id AS studentId,joined_at AS joinedAt FROM enrollments WHERE class_id=#{classId}")
    List<Enrollment> findByClass(Long classId);
}
