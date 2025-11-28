/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description AssignmentMapper
 */
package com.tiqs.mapper;

import com.tiqs.entity.Assignment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AssignmentMapper {
    @Insert("INSERT INTO assignments(course_id, title, description, deadline, created_at) VALUES(#{courseId},#{title},#{description},#{deadline},NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Assignment a);

    @Select("SELECT id,course_id AS courseId,title,description,deadline,created_at AS createdAt FROM assignments WHERE id=#{id}")
    Assignment findById(Long id);

    @Select("SELECT id,course_id AS courseId,title,description,deadline,created_at AS createdAt FROM assignments WHERE course_id=#{courseId}")
    List<Assignment> findByCourse(Long courseId);

    @Update("UPDATE assignments SET title=#{title},description=#{description},deadline=#{deadline} WHERE id=#{id}")
    int update(Assignment a);

    @Delete("DELETE FROM assignments WHERE id=#{id}")
    int delete(Long id);
}
