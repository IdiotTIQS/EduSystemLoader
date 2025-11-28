/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description ProfileRequest
 */
package com.tiqs.dto;

import lombok.Data;

@Data
public class ProfileRequest {
    private String realName;
    private String email;
    private String phone;
}
