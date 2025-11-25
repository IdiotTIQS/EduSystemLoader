package com.tiqs.auth;

public enum UserRole {
    TEACHER,
    STUDENT;

    public static UserRole from(String value) {
        for (UserRole role : values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new AuthException("未知角色:  " + value);
    }
}
