/**
 * @author TIQS
 * @date Created in 2025-11-29 00:00:00
 * @description AI模型控制器
 */
package com.tiqs.controller;

import com.tiqs.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AIModelController {

    @GetMapping("/models")
    public ApiResponse<List<String>> getAvailableModels() {
        List<String> models = Arrays.asList(
            "qwen3-coder-plus",
            "qwen3-vl-plus", 
            "qwen3-max",
            "glm-4.6",
            "deepseek-v3.2",
            "tstars2.0"
        );
        return ApiResponse.ok(models);
    }
}