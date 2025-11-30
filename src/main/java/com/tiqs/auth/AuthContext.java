/**
 * @author TIQS
 * @date Created in 2025-11-28 14:48:12
 * @description AuthContext
 */
package com.tiqs.auth;

import lombok.AllArgsConstructor;






public record AuthContext(Long userId, String username, UserRole role) {
    public static AuthContext of(Long userId, String username, String role) {
        return new AuthContext(userId, username, UserRole.from(role));
    }
}
