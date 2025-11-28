/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description AuthResponse
 */
package com.tiqs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long userId;
    private String username;
    private String role;
    private String realName;
    private String email;
    private String phone;
    private String token;
}
