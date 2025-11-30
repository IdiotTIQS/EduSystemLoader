/**
 * @author TIQS
 * @date Created in 2025-11-29 00:00:00
 * @description RestTemplate配置
 */
package com.tiqs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}