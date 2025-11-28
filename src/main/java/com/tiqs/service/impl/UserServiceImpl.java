package com.tiqs.service.impl;

import com.tiqs.auth.AuthContext;
import com.tiqs.auth.AuthException;
import com.tiqs.auth.JwtTokenProvider;
import com.tiqs.auth.UserRole;
import com.tiqs.dto.AuthRequest;
import com.tiqs.dto.AuthResponse;
import com.tiqs.dto.ProfileRequest;
import com.tiqs.dto.RegisterRequest;
import com.tiqs.entity.User;
import com.tiqs.entity.UserProfile;
import com.tiqs.mapper.UserMapper;
import com.tiqs.mapper.UserProfileMapper;
import com.tiqs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现类
 *
 * @author EduSystemLoader
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validateRegisterRequest(request);
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new AuthException("用户名已存在");
        }
        UserRole role = UserRole.from(request.getRole());
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role.name());
        userMapper.insert(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setRealName(request.getRealName());
        profile.setEmail(request.getEmail());
        profile.setPhone(request.getPhone());
        if (StringUtils.hasText(request.getRealName()) || StringUtils.hasText(request.getEmail()) || StringUtils.hasText(request.getPhone())) {
            userProfileMapper.upsert(profile);
        }
        return buildResponse(user, userProfileMapper.findByUserId(user.getId()));
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new AuthException("用户名或密码不能为空");
        }
        User user = userMapper.findByUsername(request.getUsername().trim());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("用户名或密码错误");
        }
        return buildResponse(user, userProfileMapper.findByUserId(user.getId()));
    }

    @Override
    public UserProfile loadProfile(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new AuthException("用户不存在");
        }
        UserProfile profile = userProfileMapper.findByUserId(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
        }
        return profile;
    }

    @Override
    @Transactional
    public UserProfile updateProfile(Long userId, ProfileRequest request) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new AuthException("用户不存在");
        }
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setRealName(request.getRealName());
        profile.setEmail(request.getEmail());
        profile.setPhone(request.getPhone());
        userProfileMapper.upsert(profile);
        return userProfileMapper.findByUserId(userId);
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new AuthException("用户名和密码不能为空");
        }
        if (!StringUtils.hasText(request.getRole())) {
            throw new AuthException("必须指定角色");
        }
    }

    private AuthResponse buildResponse(User user, UserProfile profile) {
        AuthContext authContext = new AuthContext(
                user.getId(),
                user.getUsername(),
                UserRole.valueOf(user.getRole())
        );
        String token = jwtTokenProvider.generateToken(authContext);

        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .realName(profile != null ? profile.getRealName() : null)
                .email(profile != null ? profile.getEmail() : null)
                .phone(profile != null ? profile.getPhone() : null)
                .token(token)
                .build();
    }
}
