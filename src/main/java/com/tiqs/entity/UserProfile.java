package com.tiqs.entity;

import lombok.Data;

@Data
public class UserProfile {
    private Long userId;
    private String realName;
    private String email;
    private String phone;
}
