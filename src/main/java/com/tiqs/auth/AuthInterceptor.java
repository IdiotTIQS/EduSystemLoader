package com.tiqs.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final Set<String> WHITE_LIST = Set.of("/api/auth/login", "/api/auth/register");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        if (WHITE_LIST.contains(path)) {
            return true;
        }
        String userIdHeader = request.getHeader(HEADER_USER_ID);
        String roleHeader = request.getHeader(HEADER_USER_ROLE);
        if (!StringUtils.hasText(userIdHeader) || !StringUtils.hasText(roleHeader)) {
            throw new AuthException("缺少鉴权头: X-User-Id / X-User-Role");
        }
        Long userId = parseUserId(userIdHeader);
        UserRole role = UserRole.from(roleHeader);
        AuthContextHolder.set(new AuthContext(userId, role));

        if (handler instanceof HandlerMethod handlerMethod) {
            RequireRole requireRole = resolveAnnotation(handlerMethod);
            if (requireRole != null && requireRole.value().length > 0) {
                boolean allowed = Arrays.stream(requireRole.value()).anyMatch(r -> r == role);
                if (!allowed) {
                    throw new AuthException("当前角色无权访问该接口");
                }
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContextHolder.clear();
    }

    private Long parseUserId(String headerValue) {
        try {
            return Long.parseLong(headerValue);
        } catch (NumberFormatException ex) {
            throw new AuthException("X-User-Id 必须为数字");
        }
    }

    private RequireRole resolveAnnotation(HandlerMethod handlerMethod) {
        RequireRole method = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (method != null) {
            return method;
        }
        return handlerMethod.getBeanType().getAnnotation(RequireRole.class);
    }
}
