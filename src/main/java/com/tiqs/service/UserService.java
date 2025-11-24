package com.tiqs.service;

import com.tiqs.dto.AuthRequest;
import com.tiqs.dto.AuthResponse;
import com.tiqs.dto.ProfileRequest;
import com.tiqs.dto.RegisterRequest;
import com.tiqs.entity.UserProfile;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
    UserProfile loadProfile(Long userId);
    UserProfile updateProfile(Long userId, ProfileRequest request);
}
