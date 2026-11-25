package com.cricarena.backend.config;

import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import com.cricarena.backend.security.JwtAuthenticationFilter;
import com.cricarena.backend.security.JwtTokenProvider;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Test
    @DisplayName("FilterRegistrationBean must disable servlet container auto-registration to prevent duplicate filter execution")
    void filterRegistrationBean_mustBeDisabledForServletContainer() {
        SecurityConfig config = new SecurityConfig(jwtTokenProvider, userRepository, userRoleAssignmentRepository);
        JwtAuthenticationFilter filter = config.jwtAuthenticationFilter();
        FilterRegistrationBean<JwtAuthenticationFilter> registration = config.jwtAuthenticationFilterRegistration(filter);

        assertNotNull(registration);
        assertFalse(registration.isEnabled(), "FilterRegistrationBean must be disabled so Spring Boot doesn't register it twice!");
    }

    @Test
    @DisplayName("PasswordEncoder bean should be BCryptPasswordEncoder")
    void passwordEncoder_shouldBeBCrypt() {
        SecurityConfig config = new SecurityConfig(jwtTokenProvider, userRepository, userRoleAssignmentRepository);
        PasswordEncoder encoder = config.passwordEncoder();

        assertNotNull(encoder);
        String raw = "secretPassword";
        String encoded = encoder.encode(raw);
        assertTrue(encoder.matches(raw, encoded));
    }
}
