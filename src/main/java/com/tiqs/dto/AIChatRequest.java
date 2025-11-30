/**
 * @author TIQS
 * @date Created in 2025-11-29 00:00:00
 * @description AI问答请求DTO
 */
package com.tiqs.dto;

import lombok.Data;

@Data
public class AIChatRequest {
    private Long userId;
    private String question;
    private String context; // 可选的上下文信息
    private String model; // AI模型选择
}