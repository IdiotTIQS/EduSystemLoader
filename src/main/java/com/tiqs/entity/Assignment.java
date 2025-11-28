/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description Assignment
 */
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
