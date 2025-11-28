package com.tiqs.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Assignment {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
}
