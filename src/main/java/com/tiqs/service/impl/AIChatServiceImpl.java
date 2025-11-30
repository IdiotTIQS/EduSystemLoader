/**
 * @author TIQS
 * @date Created in 2025-11-29 00:00:00
 * @description AI问答服务实现
 */
package com.tiqs.service.impl;

import com.tiqs.dto.AIChatRequest;
import com.tiqs.dto.AIChatResponse;
import com.tiqs.service.AIChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AIChatServiceImpl implements AIChatService {
    
    @Value("${ai.api.key:}")
    private String apiKey;




    
    @Value("${ai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;
    
    private final ObjectMapper objectMapper;

    public AIChatServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    //目前来看默认选择tstar2.0是最稳定的，但这个模型也是最蠢的，回答质量不高
    @Override
    public AIChatResponse chat(AIChatRequest request) {
        try {
            // 获取选择的模型，默认使用 tstars2.0
            String selectedModel = request.getModel() != null ? request.getModel() : "tstars2.0";
            
            log.info("AI问答请求 userId={} question={} model={}", request.getUserId(), request.getQuestion(), selectedModel);
            
            // 直接调用无工具的API，避免超时
            String answer = callAIApiWithoutTools(request, selectedModel);

            AIChatResponse response = new AIChatResponse();
            response.setAnswer(answer);
            response.setTimestamp(LocalDateTime.now());
            response.setSessionId(UUID.randomUUID().toString());
            
            return response;
        } catch (Exception e) {
            log.error("AI问答调用失败", e);
            AIChatResponse response = new AIChatResponse();
            response.setAnswer("抱歉，AI服务暂时不可用，请稍后再试。");
            response.setTimestamp(LocalDateTime.now());
            response.setSessionId(UUID.randomUUID().toString());
            return response;
        }
    }

    private String buildPrompt(AIChatRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个教育助手，专门帮助学生回答作业相关问题。");
        
        if (request.getContext() != null && !request.getContext().isEmpty()) {
            prompt.append("上下文信息：").append(request.getContext()).append("\n");
        }
        
        prompt.append("学生问题：").append(request.getQuestion());
        prompt.append("\n请提供详细、准确的答案，帮助学生理解相关概念。");
        
        return prompt.toString();
    }

    private String callAIApiWithoutTools(AIChatRequest request, String model) {
        try {
            // 构建消息 - 直接使用用户问题，不包含工具调用
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", buildPrompt(request));
            
            // 构建响应格式对象
            Map<String, Object> responseFormat = new HashMap<>();
            responseFormat.put("type", "text");
            
            // 构建请求参数 - 优化性能，禁用思考模式
            Map<String, Object> apiRequestBody = new HashMap<>();
            apiRequestBody.put("model", model);
            apiRequestBody.put("messages", Arrays.asList(message));
            apiRequestBody.put("stream", false);
            apiRequestBody.put("max_tokens", 1024);
            apiRequestBody.put("temperature", 0.7);
            apiRequestBody.put("response_format", responseFormat);
            

            apiRequestBody.put("reasoning_mode", "disabled");
            apiRequestBody.put("include_reasoning", false);
            apiRequestBody.put("disable_search", true);
            apiRequestBody.put("disable_tools", true);
            
            // 转换为JSON字符串
            String jsonBody = objectMapper.writeValueAsString(apiRequestBody);
            log.info("发送AI请求(无工具): {}", jsonBody);
            
            // 使用配置的API URL
            String targetUrl = apiUrl;
            if (targetUrl == null || targetUrl.isEmpty()) {
                targetUrl = "https://apis.iflow.cn/v1/chat/completions";
            }
            
            // 发送请求 - 增加超时设置
            HttpResponse<String> response = Unirest.post(targetUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(jsonBody)
                .socketTimeout(120000)
                .connectTimeout(30000)
                .asString();
            
            log.info("AI响应状态码(无工具): {}, 响应内容: {}", response.getStatus(), response.getBody());
            
            if (response.getStatus() == 200) {

                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                Map<String, Object> choices = (Map<String, Object>) ((java.util.List<?>) responseMap.get("choices")).get(0);
                Map<String, Object> messageResponse = (Map<String, Object>) choices.get("message");
                

                String content = (String) messageResponse.get("content");
                String reasoningContent = (String) messageResponse.get("reasoning_content");
                

                if (content == null || content.trim().isEmpty() || 
                    content.contains("用户问的是") || content.contains("让我调用")) {
                    log.info("content为空或包含思考内容，尝试从reasoning_content获取答案");
                    if (reasoningContent != null && !reasoningContent.trim().isEmpty()) {
                        content = reasoningContent;
                    }
                }

                if (content == null || content.trim().isEmpty()) {
                    content = "抱歉，AI没有提供有效回答。";
                }
                
                log.info("最终获取到的内容长度: {}", content.length());
                return content;
            } else {
                log.error("AI API调用失败（无工具），状态码: {}, 响应: {}", response.getStatus(), response.getBody());
                return "抱歉，AI服务调用失败，请稍后再试。";
            }
        } catch (Exception e) {
            log.error("AI API调用异常（无工具）", e);
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }

    
    
}