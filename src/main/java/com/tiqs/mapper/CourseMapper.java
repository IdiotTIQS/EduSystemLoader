package com.tiqs.mapper;

import com.tiqs.entity.Course;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CourseMapper {
    @Insert("INSERT INTO courses(title, description, class_id, teacher_id, created_at) VALUES(#{title},#{description},#{classId},#{teacherId},NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Course c);

    @Select("SELECT id,title,description,class_id AS classId,teacher_id AS teacherId,created_at AS createdAt FROM courses WHERE id=#{id}")
    Course findById(Long id);

    @Select("SELECT id,title,description,class_id AS classId,teacher_id AS teacherId,created_at AS createdAt FROM courses WHERE class_id=#{classId}")
    List<Course> findByClass(Long classId);

    @Update("UPDATE courses SET title=#{title},description=#{description} WHERE id=#{id}")
    int update(Course c);

    @Delete("DELETE FROM courses WHERE id=#{id}")
    int delete(Long id);
}
