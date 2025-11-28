/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description AuthContextHolder
 */
package com.tiqs.auth;

public final class AuthContextHolder {
    private static final ThreadLocal<AuthContext> CONTEXT = new ThreadLocal<>();

    private AuthContextHolder() {
    }

    public static void set(AuthContext context) {
        CONTEXT.set(context);
    }

    public static AuthContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
