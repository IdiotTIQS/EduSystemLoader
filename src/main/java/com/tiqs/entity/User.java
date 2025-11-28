package com.tiqs.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author TIQS
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
