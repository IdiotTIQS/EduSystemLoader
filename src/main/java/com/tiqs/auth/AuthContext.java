package com.tiqs.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthContext {
    private final Long userId;
    private final UserRole role;
}
