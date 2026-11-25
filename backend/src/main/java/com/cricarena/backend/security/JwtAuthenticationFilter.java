package com.cricarena.backend.security;

import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserRepository userRepository,
                                   UserRoleAssignmentRepository userRoleAssignmentRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);

            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                String email = jwtTokenProvider.getEmailFromToken(token);
                String tokenRole = jwtTokenProvider.getRoleFromToken(token);

                if (userId != null && StringUtils.hasText(email) && StringUtils.hasText(tokenRole)
                        && SecurityContextHolder.getContext().getAuthentication() == null) {

                    Optional<User> userOpt = userRepository.findById(userId);

                    if (userOpt.isPresent()) {
                        User user = userOpt.get();

                        if (user.isActive() && user.getEmail().equalsIgnoreCase(email.trim())) {
                            Optional<UserRoleAssignment> roleAssignmentOpt =
                                    userRoleAssignmentRepository.findByUser_IdAndActiveTrue(userId);

                            if (roleAssignmentOpt.isPresent()) {
                                UserRoleAssignment roleAssignment = roleAssignmentOpt.get();

                                if (roleAssignment.isActive() &&
                                        roleAssignment.getRole() != null &&
                                        roleAssignment.getRole().name().equals(tokenRole)) {

                                    SimpleGrantedAuthority authority =
                                            new SimpleGrantedAuthority("ROLE_" + tokenRole);

                                    UsernamePasswordAuthenticationToken authentication =
                                            new UsernamePasswordAuthenticationToken(
                                                    user.getEmail(),
                                                    null,
                                                    List.of(authority)
                                            );
                                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                                    SecurityContextHolder.getContext().setAuthentication(authentication);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.debug("JWT authentication error: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}
