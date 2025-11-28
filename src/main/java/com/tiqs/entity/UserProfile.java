/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description UserProfile
 */
package com.tiqs.entity;

import lombok.Data;

@Data
public class UserProfile {
    private Long userId;
    private String realName;
    private String email;
    private String phone;
}
