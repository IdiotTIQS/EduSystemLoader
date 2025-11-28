/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description DiscussionServiceImpl
 */
package com.tiqs.service.impl;

import com.tiqs.common.BusinessException;
import com.tiqs.entity.Discussion;
import com.tiqs.mapper.ClassMapper;
import com.tiqs.mapper.DiscussionMapper;
import com.tiqs.service.DiscussionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class DiscussionServiceImpl implements DiscussionService {
    private final DiscussionMapper discussionMapper;
    private final ClassMapper classMapper;

    public DiscussionServiceImpl(DiscussionMapper discussionMapper, ClassMapper classMapper) {
        this.discussionMapper = discussionMapper;
        this.classMapper = classMapper;
    }

    @Transactional
    @Override
    public Discussion create(Discussion discussion) {
        discussion.setViewCount(0);
        discussion.setIsPinned(false);
        discussionMapper.insert(discussion);
        log.info("创建讨论成功 id={} classId={} title={}", discussion.getId(), discussion.getClassId(), discussion.getTitle());
        return discussion;
    }
    @Override
    public List<Discussion> listByClass(Long classId) {
        log.debug("查询班级讨论列表 classId={}", classId);
        return discussionMapper.findByClassId(classId);
    }
    @Override
    public Discussion get(Long id) {
        Discussion discussion = discussionMapper.findById(id);
        if (discussion == null) {
            log.warn("讨论不存在 id={}", id);
            throw BusinessException.of(404, "讨论不存在");
        }
        return discussion;
    }

    @Transactional
    @Override
    public Discussion update(Discussion discussion) {
        Discussion existing = discussionMapper.findById(discussion.getId());
        if (existing == null) {
            log.warn("更新失败，讨论不存在 id={}", discussion.getId());
            throw BusinessException.of(404, "讨论不存在");
        }
        discussionMapper.update(discussion);
        log.info("更新讨论成功 id={} title={}", discussion.getId(), discussion.getTitle());
        return discussionMapper.findById(discussion.getId());
    }

    @Transactional
    @Override
    public void delete(Long id, Long userId) {
        Discussion discussion = discussionMapper.findById(id);
        if (discussion == null) {
            log.warn("删除失败，讨论不存在 id={}", id);
            throw BusinessException.of(404, "讨论不存在");
        }
        if (!discussion.getAuthorId().equals(userId)) {
            log.warn("删除失败，无权限 userId={} discussionId={} authorId={}", userId, id, discussion.getAuthorId());
            throw BusinessException.of(403, "无权限删除此讨论");
        }
        discussionMapper.delete(id);
        log.info("删除讨论成功 id={}", id);
    }

    @Transactional
    @Override
    public void incrementViewCount(Long id) {
        discussionMapper.incrementViewCount(id);
    }

    @Transactional
    @Override
    public Discussion updatePinnedStatus(Long id, Boolean isPinned, Long userId) {
        Discussion discussion = discussionMapper.findById(id);
        if (discussion == null) {
            log.warn("置顶操作失败，讨论不存在 id={}", id);
            throw BusinessException.of(404, "讨论不存在");
        }
        discussionMapper.updatePinnedStatus(id, isPinned);
        log.info("更新讨论置顶状态成功 id={} isPinned={} userId={}", id, isPinned, userId);
        return discussionMapper.findById(id);
    }
}
