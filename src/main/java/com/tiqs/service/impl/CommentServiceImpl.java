/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description CommentServiceImpl
 */
package com.tiqs.service.impl;

import com.tiqs.common.BusinessException;
import com.tiqs.entity.Comment;
import com.tiqs.mapper.CommentMapper;
import com.tiqs.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @Transactional
    @Override
    public Comment create(Comment comment) {
        comment.setIsEdited(false);
        commentMapper.insert(comment);
        log.info("创建评论成功 id={} discussionId={} authorId={}", comment.getId(), comment.getDiscussionId(), comment.getAuthorId());
        return comment;
    }
    @Override
    public List<Comment> listByDiscussion(Long discussionId) {
        log.debug("查询讨论评论列表 discussionId={}", discussionId);
        return commentMapper.findByDiscussionId(discussionId);
    }

    @Transactional
    @Override
    public Comment update(Comment comment) {
        Comment existing = commentMapper.findById(comment.getId());
        if (existing == null) {
            log.warn("更新失败，评论不存在 id={}", comment.getId());
            throw BusinessException.of(404, "评论不存在");
        }
        if (!existing.getAuthorId().equals(comment.getAuthorId())) {
            log.warn("更新失败，无权限 userId={} commentId={} authorId={}", comment.getAuthorId(), comment.getId(), existing.getAuthorId());
            throw BusinessException.of(403, "无权限修改此评论");
        }
        commentMapper.update(comment);
        log.info("更新评论成功 id={}", comment.getId());
        return commentMapper.findById(comment.getId());
    }

    @Transactional
    @Override
    public void delete(Long id, Long userId) {
        Comment comment = commentMapper.findById(id);
        if (comment == null) {
            log.warn("删除失败，评论不存在 id={}", id);
            throw BusinessException.of(404, "评论不存在");
        }
        if (!comment.getAuthorId().equals(userId)) {
            log.warn("删除失败，无权限 userId={} commentId={} authorId={}", userId, id, comment.getAuthorId());
            throw BusinessException.of(403, "无权限删除此评论");
        }
        commentMapper.delete(id);
        log.info("删除评论成功 id={}", id);
    }

    @Transactional
    @Override
    public void deleteByDiscussion(Long discussionId) {
        commentMapper.deleteByDiscussionId(discussionId);
        log.info("删除讨论下的所有评论 discussionId={}", discussionId);
    }
}
