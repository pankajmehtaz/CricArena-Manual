package com.cricarena.backend.security;

import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider, userRepository, userRoleAssignmentRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate successfully when valid JWT token is provided")
    void doFilterInternal_withValidToken_setsAuthentication() throws ServletException, IOException {
        String token = "valid.jwt.token";
        Long userId = 1L;
        String email = "admin@cricarena.com";
        String role = "PLATFORM_ADMIN";

        User user = new User("Admin", email, "hash", true);
        user.setId(userId);

        UserRoleAssignment roleAssignment = new UserRoleAssignment(user, UserRole.PLATFORM_ADMIN, null, null, true);
        roleAssignment.setId(10L);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtTokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(jwtTokenProvider.getRoleFromToken(token)).thenReturn(role);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(userId)).thenReturn(Optional.of(roleAssignment));

        filter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(email, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate when Authorization header is missing")
    void doFilterInternal_withoutAuthHeader_doesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtTokenProvider, never()).validateToken(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate when Authorization header does not start with Bearer")
    void doFilterInternal_withNonBearerHeader_doesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtTokenProvider, never()).validateToken(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate when token is invalid or expired")
    void doFilterInternal_withInvalidToken_doesNotSetAuthentication() throws ServletException, IOException {
        String token = "invalid.token";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userRepository, never()).findById(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate when user is inactive in database")
    void doFilterInternal_withInactiveUser_doesNotSetAuthentication() throws ServletException, IOException {
        String token = "valid.token";
        Long userId = 2L;
        String email = "inactive@cricarena.com";

        User inactiveUser = new User("Inactive", email, "hash", false);
        inactiveUser.setId(userId);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtTokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(jwtTokenProvider.getRoleFromToken(token)).thenReturn("COLLEGE_ADMIN");
        when(userRepository.findById(userId)).thenReturn(Optional.of(inactiveUser));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userRoleAssignmentRepository, never()).findByUser_IdAndActiveTrue(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate when user has no active role assignment in database")
    void doFilterInternal_withNoActiveRoleAssignment_doesNotSetAuthentication() throws ServletException, IOException {
        String token = "valid.token";
        Long userId = 3L;
        String email = "norole@cricarena.com";

        User user = new User("NoRole", email, "hash", true);
        user.setId(userId);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtTokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(jwtTokenProvider.getRoleFromToken(token)).thenReturn("TEAM_CAPTAIN");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(userId)).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate when active role in DB does not match token role claim")
    void doFilterInternal_withRoleMismatch_doesNotSetAuthentication() throws ServletException, IOException {
        String token = "valid.token";
        Long userId = 4L;
        String email = "mismatch@cricarena.com";

        User user = new User("User", email, "hash", true);
        user.setId(userId);

        // Role in DB is TEAM_CAPTAIN, but token claims PLATFORM_ADMIN
        UserRoleAssignment roleAssignment = new UserRoleAssignment(user, UserRole.TEAM_CAPTAIN, null, null, true);
        roleAssignment.setId(40L);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtTokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(jwtTokenProvider.getRoleFromToken(token)).thenReturn("PLATFORM_ADMIN");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(userId)).thenReturn(Optional.of(roleAssignment));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should catch unexpected exceptions safely without exposing details and clear context")
    void doFilterInternal_whenExceptionThrown_clearsContextSafely() throws ServletException, IOException {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenThrow(new RuntimeException("Simulated error"));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should authenticate role-less user with ROLE_USER authority")
    void doFilterInternal_withRolelessUserToken_setsRoleUserAuthentication() throws ServletException, IOException {
        String token = "roleless.jwt.token";
        Long userId = 10L;
        String email = "student@cricarena.com";

        User user = new User("Student", email, "hash", true);
        user.setId(userId);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtTokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(jwtTokenProvider.getRoleFromToken(token)).thenReturn(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(userId)).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(email, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        verify(filterChain).doFilter(request, response);
    }
}
