/**
 * @author TIQS
 * @date Created in 2025-11-28 14:48:12
 * @description AuthRequest
 */
package com.tiqs.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
