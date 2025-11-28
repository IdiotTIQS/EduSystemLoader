/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description Course
 */
package com.tiqs.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Course {
    private Long id;
    private String title;
    private String description;
    private Long classId;
    private Long teacherId;
    private LocalDateTime createdAt;
}
