package com.tiqs.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String role; // TEACHER / STUDENT
    private LocalDateTime createdAt;
}
