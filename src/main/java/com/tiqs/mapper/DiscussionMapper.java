package com.tiqs.mapper;

import com.tiqs.entity.Discussion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DiscussionMapper {
    @Insert("INSERT INTO discussions(class_id, title, content, author_id, is_pinned, view_count, created_at, updated_at) " +
            "VALUES(#{classId}, #{title}, #{content}, #{authorId}, #{isPinned}, #{viewCount}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Discussion discussion);

    @Select("SELECT d.id, d.class_id AS classId, d.title, d.content, d.author_id AS authorId, " +
            "d.is_pinned AS isPinned, d.view_count AS viewCount, d.created_at AS createdAt, d.updated_at AS updatedAt, " +
            "u.username AS authorName, COUNT(c.id) AS commentCount " +
            "FROM discussions d " +
            "LEFT JOIN users u ON d.author_id = u.id " +
            "LEFT JOIN comments c ON d.id = c.discussion_id " +
            "WHERE d.id = #{id} " +
            "GROUP BY d.id")
    Discussion findById(Long id);

    @Select("SELECT d.id, d.class_id AS classId, d.title, d.content, d.author_id AS authorId, " +
            "d.is_pinned AS isPinned, d.view_count AS viewCount, d.created_at AS createdAt, d.updated_at AS updatedAt, " +
            "u.username AS authorName, COUNT(c.id) AS commentCount " +
            "FROM discussions d " +
            "LEFT JOIN users u ON d.author_id = u.id " +
            "LEFT JOIN comments c ON d.id = c.discussion_id " +
            "WHERE d.class_id = #{classId} " +
            "GROUP BY d.id " +
            "ORDER BY d.is_pinned DESC, d.updated_at DESC")
    List<Discussion> findByClassId(Long classId);

    @Update("UPDATE discussions SET title=#{title}, content=#{content}, updated_at=NOW() WHERE id=#{id}")
    int update(Discussion discussion);

    @Delete("DELETE FROM discussions WHERE id=#{id}")
    int delete(Long id);

    @Update("UPDATE discussions SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);

    @Update("UPDATE discussions SET is_pinned=#{isPinned}, updated_at=NOW() WHERE id=#{id}")
    int updatePinnedStatus(@Param("id") Long id, @Param("isPinned") Boolean isPinned);
}
