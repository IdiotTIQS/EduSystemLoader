package com.tiqs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author TIQS
 * @date Created in 2025-11-28 14:48:12
 * @description SecurityConfig
 */
@Configuration
public class SecurityConfig {

    /**
     * 密码编码器Bean
     *
     * @return BCrypt密码编码器
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
