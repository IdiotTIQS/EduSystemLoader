/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description Submission
 */
package com.tiqs.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Submission {
    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String filePath;
    private String originalFileName;
    private String answerText;
    private LocalDateTime submittedAt;
    private Double score;
    private String feedback;
    private LocalDateTime gradedAt;
    private String status; // SUBMITTED / GRADED
}
