/**
 * @author TIQS
 * @date Created in 2025-11-29 00:00:00
 * @description AI问答服务接口
 */
package com.tiqs.service;

import com.tiqs.dto.AIChatRequest;
import com.tiqs.dto.AIChatResponse;

public interface AIChatService {
    AIChatResponse chat(AIChatRequest request);
}