/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description ClassEntity
 */
package com.tiqs.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClassEntity {
    private Long id;
    private String name;
    private String code;
    private Long teacherId;
    private LocalDateTime createdAt;
}
