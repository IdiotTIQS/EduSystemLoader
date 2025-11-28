/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description RequireRole
 */
package com.tiqs.auth;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    UserRole[] value() default {};
}
