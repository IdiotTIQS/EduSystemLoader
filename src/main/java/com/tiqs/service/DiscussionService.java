package com.tiqs.service;

import com.tiqs.entity.Discussion;

import java.util.List;

public interface DiscussionService {
    Discussion create(Discussion discussion);

    List<Discussion> listByClass(Long classId);

    Discussion get(Long id);

    Discussion update(Discussion discussion);

    void delete(Long id, Long userId);

    void incrementViewCount(Long id);

    Discussion updatePinnedStatus(Long id, Boolean isPinned, Long userId);
}
