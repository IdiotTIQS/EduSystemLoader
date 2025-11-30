/**
 * @author TIQS
 * @date Created in 2025-11-29 00:00:00
 * @description AI问答控制器
 */
package com.tiqs.controller;

import com.tiqs.common.ApiResponse;
import com.tiqs.dto.AIChatRequest;
import com.tiqs.dto.AIChatResponse;
import com.tiqs.service.AIChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AIChatController {
    private final AIChatService aiChatService;

    public AIChatController(AIChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    public ApiResponse<AIChatResponse> chat(@RequestBody AIChatRequest request) {
        AIChatResponse response = aiChatService.chat(request);
        return ApiResponse.ok(response);
    }
}