/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description Enrollment
 */
package com.tiqs.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Enrollment {
    private Long id;
    private Long classId;
    private Long studentId;
    private LocalDateTime joinedAt;
}
