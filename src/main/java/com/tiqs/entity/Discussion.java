package com.tiqs.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Discussion {
    private Long id;
    private Long classId;
    private String title;
    private String content;
    private Long authorId;
    private Boolean isPinned;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String authorName;
    private Integer commentCount;
}
