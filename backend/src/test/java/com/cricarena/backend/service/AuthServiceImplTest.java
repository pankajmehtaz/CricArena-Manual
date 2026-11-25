package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.LoginRequest;
import com.cricarena.backend.dto.response.AuthResponse;
import com.cricarena.backend.dto.response.UserResponse;
import com.cricarena.backend.dto.response.UserRoleAssignmentResponse;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.mapper.UserMapper;
import com.cricarena.backend.mapper.UserRoleAssignmentMapper;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import com.cricarena.backend.security.JwtTokenProvider;
import com.cricarena.backend.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                passwordEncoder,
                jwtTokenProvider,
                userMapper,
                userRoleAssignmentMapper
        );
    }

    @Test
    @DisplayName("Should login successfully with valid credentials and normalized email")
    void login_withValidCredentials_returnsAuthResponse() {
        // Arrange
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
        when(jwtTokenProvider.generateToken(1L, normalizedEmail, UserRole.PLATFORM_ADMIN)).thenReturn("mock.jwt.token");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(userMapper.toResponse(user)).thenReturn(userResponse);
        when(userRoleAssignmentMapper.toResponse(roleAssignment)).thenReturn(roleResponse);

        // Act
        LoginRequest request = new LoginRequest(rawEmail, password);
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getAccessToken());
        assertNull(response.getRefreshToken());
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
    @DisplayName("Should reject user without active role with BadCredentialsException")
    void login_withUserWithoutActiveRole_throwsBadCredentialsException() {
        User user = new User("Roleless User", "norole@cricarena.com", "hash", true);
        user.setId(4L);

        when(userRepository.findByEmail("norole@cricarena.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hash")).thenReturn(true);
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(4L)).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("norole@cricarena.com", "password");

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("User has no active role assignment", exception.getMessage());
        verify(jwtTokenProvider, never()).generateToken(any(), any(), any(UserRole.class));
    }
}
