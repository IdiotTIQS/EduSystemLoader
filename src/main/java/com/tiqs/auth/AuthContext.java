package com.tiqs.auth;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public record AuthContext(Long userId, String username, UserRole role) {
}
