package com.tiqs.mapper;

import com.tiqs.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {
    @Insert("INSERT INTO comments(discussion_id, parent_id, content, author_id, is_edited, created_at, updated_at) " +
            "VALUES(#{discussionId}, #{parentId}, #{content}, #{authorId}, #{isEdited}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    @Select("SELECT c.id, c.discussion_id AS discussionId, c.parent_id AS parentId, c.content, " +
            "c.author_id AS authorId, c.is_edited AS isEdited, c.created_at AS createdAt, c.updated_at AS updatedAt, " +
            "u.username AS authorName, u.role AS authorRole " +
            "FROM comments c " +
            "LEFT JOIN users u ON c.author_id = u.id " +
            "WHERE c.id = #{id}")
    Comment findById(Long id);

    @Select("SELECT c.id, c.discussion_id AS discussionId, c.parent_id AS parentId, c.content, " +
            "c.author_id AS authorId, c.is_edited AS isEdited, c.created_at AS createdAt, c.updated_at AS updatedAt, " +
            "u.username AS authorName, u.role AS authorRole " +
            "FROM comments c " +
            "LEFT JOIN users u ON c.author_id = u.id " +
            "WHERE c.discussion_id = #{discussionId} " +
            "ORDER BY c.created_at ASC")
    List<Comment> findByDiscussionId(Long discussionId);

    @Update("UPDATE comments SET content=#{content}, is_edited=TRUE, updated_at=NOW() WHERE id=#{id}")
    int update(Comment comment);

    @Delete("DELETE FROM comments WHERE id=#{id}")
    int delete(Long id);

    @Delete("DELETE FROM comments WHERE discussion_id=#{discussionId}")
    int deleteByDiscussionId(Long discussionId);
}
