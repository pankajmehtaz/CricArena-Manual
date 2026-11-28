package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.request.LoginRequest;
import com.cricarena.backend.dto.request.RefreshTokenRequest;
import com.cricarena.backend.dto.request.RegisterRequest;
import com.cricarena.backend.dto.response.AuthResponse;
import com.cricarena.backend.dto.response.UserResponse;
import com.cricarena.backend.dto.response.UserRoleAssignmentResponse;
import com.cricarena.backend.entity.RefreshToken;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.mapper.UserMapper;
import com.cricarena.backend.mapper.UserRoleAssignmentMapper;
import com.cricarena.backend.repository.RefreshTokenRepository;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import com.cricarena.backend.security.JwtTokenProvider;
import com.cricarena.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final UserRoleAssignmentMapper userRoleAssignmentMapper;

    @Value("${jwt.refresh-token-expiration-days:7}")
    private int refreshTokenExpirationDays = 7;

    public AuthServiceImpl(UserRepository userRepository,
                           UserRoleAssignmentRepository userRoleAssignmentRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           UserMapper userMapper,
                           UserRoleAssignmentMapper userRoleAssignmentMapper) {
        this.userRepository = userRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userMapper = userMapper;
        this.userRoleAssignmentMapper = userRoleAssignmentMapper;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null || request.getFullName() == null) {
            throw new IllegalArgumentException("Registration parameters cannot be null");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("User already exists with email: " + normalizedEmail);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getFullName().trim(), normalizedEmail, encodedPassword, true);
        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                (String) null
        );

        String rawRefreshToken = createAndSaveRefreshToken(savedUser);
        UserResponse userResponse = userMapper.toResponse(savedUser);

        return new AuthResponse(
                accessToken,
                rawRefreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationMs(),
                userResponse,
                null
        );
    }

    @Override
    @Transactional
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

        Optional<UserRoleAssignment> roleAssignmentOpt =
                userRoleAssignmentRepository.findByUser_IdAndActiveTrue(user.getId());
        UserRoleAssignment roleAssignment = roleAssignmentOpt.orElse(null);

        String roleName = (roleAssignment != null && roleAssignment.getRole() != null)
                ? roleAssignment.getRole().name()
                : null;

        String accessToken = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                roleName
        );

        String rawRefreshToken = createAndSaveRefreshToken(user);

        UserResponse userResponse = userMapper.toResponse(user);
        UserRoleAssignmentResponse roleAssignmentResponse = roleAssignment != null
                ? userRoleAssignmentMapper.toResponse(roleAssignment)
                : null;

        return new AuthResponse(
                accessToken,
                rawRefreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationMs(),
                userResponse,
                roleAssignmentResponse
        );
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
            throw new BadCredentialsException("Refresh token is required");
        }

        String rawToken = request.getRefreshToken().trim();
        String tokenHash = hashToken(rawToken);

        RefreshToken tokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired refresh token"));

        if (tokenEntity.isRevoked() || tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        User user = tokenEntity.getUser();
        if (!user.isActive()) {
            throw new BadCredentialsException("User account is inactive");
        }

        // Token rotation: revoke previous refresh token
        tokenEntity.setRevoked(true);
        tokenEntity.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(tokenEntity);

        // Issue new rotated refresh token
        String newRawRefreshToken = createAndSaveRefreshToken(user);

        // Lookup current active role if any
        Optional<UserRoleAssignment> roleAssignmentOpt =
                userRoleAssignmentRepository.findByUser_IdAndActiveTrue(user.getId());
        UserRoleAssignment roleAssignment = roleAssignmentOpt.orElse(null);

        String roleName = (roleAssignment != null && roleAssignment.getRole() != null)
                ? roleAssignment.getRole().name()
                : null;

        String accessToken = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                roleName
        );

        UserResponse userResponse = userMapper.toResponse(user);
        UserRoleAssignmentResponse roleAssignmentResponse = roleAssignment != null
                ? userRoleAssignmentMapper.toResponse(roleAssignment)
                : null;

        return new AuthResponse(
                accessToken,
                newRawRefreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationMs(),
                userResponse,
                roleAssignmentResponse
        );
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
            return;
        }

        String rawToken = request.getRefreshToken().trim();
        String tokenHash = hashToken(rawToken);

        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(tokenEntity -> {
            if (!tokenEntity.isRevoked()) {
                tokenEntity.setRevoked(true);
                tokenEntity.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(tokenEntity);
            }
        });
    }

    private String createAndSaveRefreshToken(User user) {
        String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(refreshTokenExpirationDays);

        RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiresAt, false);
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
