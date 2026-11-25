package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.request.LoginRequest;
import com.cricarena.backend.dto.request.RefreshTokenRequest;
import com.cricarena.backend.dto.response.AuthResponse;
import com.cricarena.backend.dto.response.UserResponse;
import com.cricarena.backend.dto.response.UserRoleAssignmentResponse;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.mapper.UserMapper;
import com.cricarena.backend.mapper.UserRoleAssignmentMapper;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import com.cricarena.backend.security.JwtTokenProvider;
import com.cricarena.backend.service.AuthService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final UserRoleAssignmentMapper userRoleAssignmentMapper;

    public AuthServiceImpl(UserRepository userRepository,
                           UserRoleAssignmentRepository userRoleAssignmentRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           UserMapper userMapper,
                           UserRoleAssignmentMapper userRoleAssignmentMapper) {
        this.userRepository = userRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userMapper = userMapper;
        this.userRoleAssignmentMapper = userRoleAssignmentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new BadCredentialsException("User account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        UserRoleAssignment roleAssignment = userRoleAssignmentRepository.findByUser_IdAndActiveTrue(user.getId())
                .orElseThrow(() -> new BadCredentialsException("User has no active role assignment"));

        String accessToken = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                roleAssignment.getRole()
        );

        UserResponse userResponse = userMapper.toResponse(user);
        UserRoleAssignmentResponse roleAssignmentResponse = userRoleAssignmentMapper.toResponse(roleAssignment);

        return new AuthResponse(
                accessToken,
                null,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationMs(),
                userResponse,
                roleAssignmentResponse
        );
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        throw new UnsupportedOperationException("Refresh token functionality is not implemented yet.");
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        throw new UnsupportedOperationException("Logout functionality is not implemented yet.");
    }
}
