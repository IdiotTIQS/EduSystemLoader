/**
 * @author TIQS
 * @date Created in 2025-11-29 00:00:00
 * @description AI问答响应DTO
 */
package com.tiqs.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AIChatResponse {
    private String answer;
    private LocalDateTime timestamp;
    private String sessionId;
}