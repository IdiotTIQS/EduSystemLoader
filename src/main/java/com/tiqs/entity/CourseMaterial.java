package com.tiqs.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseMaterial {
    private Long id;
    private Long courseId;
    private String type;
    private String name;
    private String url;
    private String content;
    private LocalDateTime createdAt;
}
