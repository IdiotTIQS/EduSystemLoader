package com.tiqs.controller;

import com.tiqs.common.ApiResponse;
import com.tiqs.dto.AuthRequest;
import com.tiqs.dto.AuthResponse;
import com.tiqs.dto.RegisterRequest;
import com.tiqs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author TIQS
 * @date Created in 2025-11-28 14:48:12
 * @description AuthController
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    /**
     * 用户注册
     *
     * @param request 注册请求信息
     * @return 认证响应信息
     */
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ApiResponse.ok(userService.register(request));
    }

    /**
     * 用户登录
     *
     * @param request 登录请求信息
     * @return 认证响应信息
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody AuthRequest request) {
        return ApiResponse.ok(userService.login(request));
    }
}
