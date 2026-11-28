package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.LoginRequest;
import com.cricarena.backend.dto.request.RefreshTokenRequest;
import com.cricarena.backend.dto.request.RegisterRequest;
import com.cricarena.backend.dto.response.AuthResponse;
import com.cricarena.backend.dto.response.UserResponse;
import com.cricarena.backend.dto.response.UserRoleAssignmentResponse;
import com.cricarena.backend.entity.RefreshToken;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.mapper.UserMapper;
import com.cricarena.backend.mapper.UserRoleAssignmentMapper;
import com.cricarena.backend.repository.RefreshTokenRepository;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import com.cricarena.backend.security.JwtTokenProvider;
import com.cricarena.backend.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRoleAssignmentMapper userRoleAssignmentMapper;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                userRoleAssignmentRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtTokenProvider,
                userMapper,
                userRoleAssignmentMapper
        );
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Should login successfully with valid credentials and normalized email")
    void login_withValidCredentials_returnsAuthResponse() {
        String rawEmail = "  Admin@CricArena.COM  ";
        String normalizedEmail = "admin@cricarena.com";
        String password = "CorrectPassword123";
        String passwordHash = "$2a$10$hashedPasswordValue";

        User user = new User("Admin User", normalizedEmail, passwordHash, true);
        user.setId(1L);

        UserRoleAssignment roleAssignment = new UserRoleAssignment(user, UserRole.PLATFORM_ADMIN, null, null, true);
        roleAssignment.setId(10L);

        UserResponse userResponse = new UserResponse(1L, "Admin User", normalizedEmail, true, null, null);
        UserRoleAssignmentResponse roleResponse = new UserRoleAssignmentResponse();
        roleResponse.setId(10L);
        roleResponse.setRole(UserRole.PLATFORM_ADMIN);

        when(userRepository.findByEmail(normalizedEmail)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, passwordHash)).thenReturn(true);
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(1L)).thenReturn(Optional.of(roleAssignment));
        when(jwtTokenProvider.generateToken(1L, normalizedEmail, "PLATFORM_ADMIN")).thenReturn("mock.jwt.token");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(userMapper.toResponse(user)).thenReturn(userResponse);
        when(userRoleAssignmentMapper.toResponse(roleAssignment)).thenReturn(roleResponse);

        LoginRequest request = new LoginRequest(rawEmail, password);
        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900000L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals(1L, response.getUser().getId());
        assertEquals(normalizedEmail, response.getUser().getEmail());
        assertNotNull(response.getRoleAssignment());
        assertEquals(UserRole.PLATFORM_ADMIN, response.getRoleAssignment().getRole());

        verify(userRepository).findByEmail(normalizedEmail);
        verify(passwordEncoder).matches(password, passwordHash);
        verify(userRoleAssignmentRepository).findByUser_IdAndActiveTrue(1L);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should login role-less normal user successfully with roleAssignment null")
    void login_withRolelessUser_returnsAuthResponseWithNullRoleAssignment() {
        String email = "student@cricarena.com";
        String password = "Password123";
        String passwordHash = "$2a$10$hashedPasswordValue";

        User user = new User("Normal Student", email, passwordHash, true);
        user.setId(5L);

        UserResponse userResponse = new UserResponse(5L, "Normal Student", email, true, null, null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, passwordHash)).thenReturn(true);
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(5L)).thenReturn(Optional.empty());
        when(jwtTokenProvider.generateToken(5L, email, (String) null)).thenReturn("mock.jwt.token.roleless");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        LoginRequest request = new LoginRequest(email, password);
        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token.roleless", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNull(response.getRoleAssignment());
        assertEquals(email, response.getUser().getEmail());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should reject unknown email with BadCredentialsException")
    void login_withUnknownEmail_throwsBadCredentialsException() {
        when(userRepository.findByEmail("unknown@cricarena.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("unknown@cricarena.com", "anyPassword");

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("Should reject inactive user with BadCredentialsException")
    void login_withInactiveUser_throwsBadCredentialsException() {
        User inactiveUser = new User("Inactive User", "inactive@cricarena.com", "hash", false);
        inactiveUser.setId(2L);

        when(userRepository.findByEmail("inactive@cricarena.com")).thenReturn(Optional.of(inactiveUser));

        LoginRequest request = new LoginRequest("inactive@cricarena.com", "anyPassword");

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("User account is inactive", exception.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("Should reject incorrect password with BadCredentialsException")
    void login_withIncorrectPassword_throwsBadCredentialsException() {
        User user = new User("User", "user@cricarena.com", "actualHash", true);
        user.setId(3L);

        when(userRepository.findByEmail("user@cricarena.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "actualHash")).thenReturn(false);

        LoginRequest request = new LoginRequest("user@cricarena.com", "wrongPassword");

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRoleAssignmentRepository, never()).findByUser_IdAndActiveTrue(any());
    }

    @Test
    @DisplayName("Should register normal user successfully and return AuthResponse")
    void register_withValidRequest_returnsAuthResponse() {
        RegisterRequest request = new RegisterRequest("New Student", "Student@CricArena.COM", "SecurePass123");
        String normalizedEmail = "student@cricarena.com";

        when(userRepository.existsByEmail(normalizedEmail)).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123")).thenReturn("$2a$10$encodedHash");

        User savedUser = new User("New Student", normalizedEmail, "$2a$10$encodedHash", true);
        savedUser.setId(15L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateToken(15L, normalizedEmail, (String) null)).thenReturn("registered.jwt.token");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);

        UserResponse userResponse = new UserResponse(15L, "New Student", normalizedEmail, true, null, null);
        when(userMapper.toResponse(savedUser)).thenReturn(userResponse);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("registered.jwt.token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNull(response.getRoleAssignment());
        assertEquals("New Student", response.getUser().getFullName());
        assertEquals(normalizedEmail, response.getUser().getEmail());

        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should reject duplicate registration email with DuplicateResourceException")
    void register_withDuplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest("Existing", "already@exists.com", "Password123");
        when(userRepository.existsByEmail("already@exists.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should refresh token with token rotation")
    void refreshToken_withValidToken_rotatesTokenAndReturnsNewAuth() {
        String rawToken = "my-valid-refresh-token";
        String tokenHash = hashToken(rawToken);

        User user = new User("User One", "user@cricarena.com", "hash", true);
        user.setId(7L);

        RefreshToken tokenEntity = new RefreshToken(user, tokenHash, LocalDateTime.now().plusDays(5), false);
        tokenEntity.setId(100L);

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(tokenEntity));
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(7L)).thenReturn(Optional.empty());
        when(jwtTokenProvider.generateToken(7L, "user@cricarena.com", (String) null)).thenReturn("rotated.jwt.token");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);

        UserResponse userResponse = new UserResponse(7L, "User One", "user@cricarena.com", true, null, null);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        RefreshTokenRequest request = new RefreshTokenRequest(rawToken);
        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("rotated.jwt.token", response.getAccessToken());
        assertTrue(tokenEntity.isRevoked());
        assertNotNull(tokenEntity.getRevokedAt());

        verify(refreshTokenRepository, org.mockito.Mockito.times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should reject revoked refresh token with BadCredentialsException")
    void refreshToken_withRevokedToken_throwsBadCredentialsException() {
        String rawToken = "revoked-token";
        String tokenHash = hashToken(rawToken);

        User user = new User("User", "user@cricarena.com", "hash", true);
        RefreshToken tokenEntity = new RefreshToken(user, tokenHash, LocalDateTime.now().plusDays(5), true);

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(tokenEntity));

        RefreshTokenRequest request = new RefreshTokenRequest(rawToken);
        assertThrows(BadCredentialsException.class, () -> authService.refreshToken(request));
    }

    @Test
    @DisplayName("Should revoke refresh token on logout")
    void logout_withValidToken_marksRevoked() {
        String rawToken = "active-token";
        String tokenHash = hashToken(rawToken);

        User user = new User("User", "user@cricarena.com", "hash", true);
        RefreshToken tokenEntity = new RefreshToken(user, tokenHash, LocalDateTime.now().plusDays(5), false);

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(tokenEntity));

        authService.logout(new RefreshTokenRequest(rawToken));

        assertTrue(tokenEntity.isRevoked());
        assertNotNull(tokenEntity.getRevokedAt());
        verify(refreshTokenRepository).save(tokenEntity);
    }
}
