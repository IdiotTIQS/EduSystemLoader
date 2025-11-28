/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description Notification
 */
package com.tiqs.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String type;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public enum NotificationType {
        ASSIGNMENT("作业通知"),
        COURSE("课程通知"),
        ENROLLMENT("选课通知"),
        GRADE("成绩通知"),
        SYSTEM("系统通知");

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}