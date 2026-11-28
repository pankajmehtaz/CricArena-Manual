package com.cricarena.backend.controller;

import com.cricarena.backend.dto.request.LoginRequest;
import com.cricarena.backend.dto.request.RefreshTokenRequest;
import com.cricarena.backend.dto.request.RegisterRequest;
import com.cricarena.backend.dto.response.AuthResponse;
import com.cricarena.backend.dto.response.UserResponse;
import com.cricarena.backend.dto.response.UserRoleAssignmentResponse;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.exception.GlobalExceptionHandler;
import com.cricarena.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/login - Success (200 OK) with valid credentials")
    void login_withValidCredentials_returns200AndValidAuthResponse() throws Exception {
        UserResponse userResponse = new UserResponse(1L, "Admin User", "admin@cricarena.com", true, null, null);
        UserRoleAssignmentResponse roleResponse = new UserRoleAssignmentResponse();
        roleResponse.setId(10L);
        roleResponse.setRole(UserRole.PLATFORM_ADMIN);

        AuthResponse authResponse = new AuthResponse(
                "header.payload.signature",
                "refresh-token-123",
                "Bearer",
                900000L,
                userResponse,
                roleResponse
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        LoginRequest request = new LoginRequest("admin@cricarena.com", "validPassword123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("header.payload.signature")))
                .andExpect(jsonPath("$.refreshToken", is("refresh-token-123")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", is(900000)))
                .andExpect(jsonPath("$.user.id", is(1)))
                .andExpect(jsonPath("$.user.email", is("admin@cricarena.com")))
                .andExpect(jsonPath("$.roleAssignment.role", is("PLATFORM_ADMIN")))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/login - Incorrect password returns 401 Unauthorized")
    void login_withIncorrectPassword_returns401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        LoginRequest request = new LoginRequest("admin@cricarena.com", "wrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("POST /api/auth/register - Success (201 Created)")
    void register_withValidRequest_returns201() throws Exception {
        UserResponse userResponse = new UserResponse(20L, "New Student", "student@cricarena.com", true, null, null);
        AuthResponse authResponse = new AuthResponse(
                "jwt.access.token",
                "new-refresh-token",
                "Bearer",
                900000L,
                userResponse,
                null
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        RegisterRequest request = new RegisterRequest("New Student", "student@cricarena.com", "ValidPass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", is("jwt.access.token")))
                .andExpect(jsonPath("$.refreshToken", is("new-refresh-token")))
                .andExpect(jsonPath("$.user.id", is(20)))
                .andExpect(jsonPath("$.user.fullName", is("New Student")))
                .andExpect(jsonPath("$.roleAssignment").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/register - Duplicate email returns 409 Conflict")
    void register_withDuplicateEmail_returns409() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("User already exists with email: student@cricarena.com"));

        RegisterRequest request = new RegisterRequest("New Student", "student@cricarena.com", "ValidPass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Success (200 OK)")
    void refresh_withValidToken_returns200() throws Exception {
        UserResponse userResponse = new UserResponse(1L, "Admin User", "admin@cricarena.com", true, null, null);
        AuthResponse authResponse = new AuthResponse(
                "new.jwt.access.token",
                "rotated-refresh-token",
                "Bearer",
                900000L,
                userResponse,
                null
        );

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(authResponse);

        RefreshTokenRequest request = new RefreshTokenRequest("existing-refresh-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("new.jwt.access.token")))
                .andExpect(jsonPath("$.refreshToken", is("rotated-refresh-token")));
    }

    @Test
    @DisplayName("POST /api/auth/logout - Success (204 No Content)")
    void logout_withRefreshToken_returns204() throws Exception {
        doNothing().when(authService).logout(any(RefreshTokenRequest.class));

        RefreshTokenRequest request = new RefreshTokenRequest("token-to-revoke");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService).logout(any(RefreshTokenRequest.class));
    }
}
