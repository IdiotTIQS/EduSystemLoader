/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description CourseMaterial
 */
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
