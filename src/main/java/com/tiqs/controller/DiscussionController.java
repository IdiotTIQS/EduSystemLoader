/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description DiscussionController
 */
package com.tiqs.controller;

import com.tiqs.auth.AuthContextHolder;
import com.tiqs.auth.RequireRole;
import com.tiqs.auth.UserRole;
import com.tiqs.common.ApiResponse;
import com.tiqs.common.BusinessException;
import com.tiqs.entity.Comment;
import com.tiqs.entity.Discussion;
import com.tiqs.service.CommentService;
import com.tiqs.service.DiscussionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {
    private final DiscussionService discussionService;
    private final CommentService commentService;

    public DiscussionController(DiscussionService discussionService, CommentService commentService) {
        this.discussionService = discussionService;
        this.commentService = commentService;
    }

    @PostMapping
    public ApiResponse<Discussion> create(@RequestBody Discussion discussion) {
        Long userId = AuthContextHolder.get().userId();
        discussion.setAuthorId(userId);
        log.info("请求创建讨论 classId={} title={}", discussion.getClassId(), discussion.getTitle());
        return ApiResponse.ok(discussionService.create(discussion));
    }

    @GetMapping("/class/{classId}")
    public ApiResponse<List<Discussion>> listByClass(@PathVariable Long classId) {
        log.debug("查询班级讨论列表 classId={}", classId);
        return ApiResponse.ok(discussionService.listByClass(classId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Discussion> get(@PathVariable Long id) {
        log.debug("获取讨论详情 id={}", id);
        Discussion discussion = discussionService.get(id);
        discussionService.incrementViewCount(id);
        return ApiResponse.ok(discussion);
    }

    @PutMapping("/{id}")
    public ApiResponse<Discussion> update(@PathVariable Long id, @RequestBody Discussion discussion) {
        Long userId = AuthContextHolder.get().userId();
        Discussion existing = discussionService.get(id);
        if (!existing.getAuthorId().equals(userId)) {
            throw BusinessException.of(403, "无权限修改此讨论");
        }
        discussion.setId(id);
        discussion.setAuthorId(userId);
        log.info("更新讨论 id={} title={}", id, discussion.getTitle());
        return ApiResponse.ok(discussionService.update(discussion));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Long userId = AuthContextHolder.get().userId();
        log.warn("删除讨论 id={} userId={}", id, userId);
        discussionService.delete(id, userId);
        return ApiResponse.ok(null);
    }

    @RequireRole(UserRole.TEACHER)
    @PutMapping("/{id}/pin")
    public ApiResponse<Discussion> updatePinnedStatus(@PathVariable Long id, @RequestParam Boolean isPinned) {
        Long userId = AuthContextHolder.get().userId();
        log.info("更新讨论置顶状态 id={} isPinned={} userId={}", id, isPinned, userId);
        return ApiResponse.ok(discussionService.updatePinnedStatus(id, isPinned, userId));
    }

    @PostMapping("/{discussionId}/comments")
    public ApiResponse<Comment> createComment(@PathVariable Long discussionId, @RequestBody Comment comment) {
        Long userId = AuthContextHolder.get().userId();
        comment.setDiscussionId(discussionId);
        comment.setAuthorId(userId);
        log.info("请求创建评论 discussionId={} content={}", discussionId, comment.getContent().substring(0, Math.min(50, comment.getContent().length())));
        return ApiResponse.ok(commentService.create(comment));
    }

    @GetMapping("/{discussionId}/comments")
    public ApiResponse<List<Comment>> listComments(@PathVariable Long discussionId) {
        log.debug("查询讨论评论列表 discussionId={}", discussionId);
        return ApiResponse.ok(commentService.listByDiscussion(discussionId));
    }

    @PutMapping("/comments/{id}")
    public ApiResponse<Comment> updateComment(@PathVariable Long id, @RequestBody Comment comment) {
        Long userId = AuthContextHolder.get().userId();
        comment.setId(id);
        comment.setAuthorId(userId);
        log.info("更新评论 id={} userId={}", id, userId);
        return ApiResponse.ok(commentService.update(comment));
    }

    @DeleteMapping("/comments/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Long id) {
        Long userId = AuthContextHolder.get().userId();
        log.warn("删除评论 id={} userId={}", id, userId);
        commentService.delete(id, userId);
        return ApiResponse.ok(null);
    }
}