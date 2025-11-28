/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description RegisterRequest
 */
package com.tiqs.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String role; // TEACHER / STUDENT
    private String realName;
    private String email;
    private String phone;
}
