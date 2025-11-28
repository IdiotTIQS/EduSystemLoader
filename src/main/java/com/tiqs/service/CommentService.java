/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description CommentService
 */
package com.tiqs.service;

import com.tiqs.entity.Comment;

import java.util.List;

public interface CommentService {
    Comment create(Comment comment);

    List<Comment> listByDiscussion(Long discussionId);

    Comment update(Comment comment);

    void delete(Long id, Long userId);

    void deleteByDiscussion(Long discussionId);
}
