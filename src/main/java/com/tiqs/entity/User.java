package com.tiqs.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author TIQS
 * @date Created in 2025-11-28 14:48:12
 * @description User
 */
@Data
public class User {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码哈希值
     */
    private String passwordHash;

    /**
     * 用户角色 (TEACHER / STUDENT)
     */
    private String role;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
