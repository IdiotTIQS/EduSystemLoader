package com.tiqs.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final Set<String> WHITE_LIST = Set.of("/api/auth/login", "/api/auth/register");
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        if (WHITE_LIST.contains(path)) {
            return true;
        }

        String authHeader = request.getHeader(HEADER_AUTHORIZATION);
        if (!StringUtils.hasText(authHeader)) {
            throw new AuthException("缺少认证头: Authorization");
        }

        String token = jwtTokenProvider.extractTokenFromBearer(authHeader);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new AuthException("无效的认证令牌");
        }

        AuthContext authContext = jwtTokenProvider.getAuthContextFromToken(token);
        AuthContextHolder.set(authContext);

        if (handler instanceof HandlerMethod handlerMethod) {
            RequireRole requireRole = resolveAnnotation(handlerMethod);
            if (requireRole != null && requireRole.value().length > 0) {
                boolean allowed = Arrays.stream(requireRole.value()).anyMatch(r -> r == authContext.role());
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


    private RequireRole resolveAnnotation(HandlerMethod handlerMethod) {
        RequireRole method = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (method != null) {
            return method;
        }
        return handlerMethod.getBeanType().getAnnotation(RequireRole.class);
    }
}
