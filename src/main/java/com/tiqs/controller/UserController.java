package com.tiqs.controller;

import com.tiqs.auth.AuthContext;
import com.tiqs.auth.AuthContextHolder;
import com.tiqs.auth.AuthException;
import com.tiqs.common.ApiResponse;
import com.tiqs.dto.ProfileRequest;
import com.tiqs.entity.UserProfile;
import com.tiqs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/profile")
    public ApiResponse<UserProfile> profile() {
        AuthContext auth = requireAuth();
        return ApiResponse.ok(userService.loadProfile(auth.getUserId()));
    }

    @PutMapping("/profile")
    public ApiResponse<UserProfile> update(@RequestBody ProfileRequest request) {
        AuthContext auth = requireAuth();
        return ApiResponse.ok(userService.updateProfile(auth.getUserId(), request));
    }

    private AuthContext requireAuth() {
        AuthContext auth = AuthContextHolder.get();
        if (auth == null) {
            throw new AuthException("未登录或登录已失效");
        }
        return auth;
    }
}
