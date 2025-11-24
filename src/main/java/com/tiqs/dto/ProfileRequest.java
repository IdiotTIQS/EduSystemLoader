package com.tiqs.dto;

import lombok.Data;

@Data
public class ProfileRequest {
    private String realName;
    private String email;
    private String phone;
}
