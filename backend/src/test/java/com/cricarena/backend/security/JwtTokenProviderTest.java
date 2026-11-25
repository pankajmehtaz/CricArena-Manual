package com.cricarena.backend.security;

import com.cricarena.backend.entity.enums.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String TEST_SECRET = "super-secret-key-that-is-at-least-256-bits-long-for-testing-purpose";
    private static final long EXPIRATION_MS = 900000; // 15 mins

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpirationMs", EXPIRATION_MS);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("Should generate valid token and extract claims correctly")
    void shouldGenerateTokenAndExtractClaims() {
        Long userId = 42L;
        String email = "admin@cricarena.com";
        UserRole role = UserRole.PLATFORM_ADMIN;

        String token = jwtTokenProvider.generateToken(userId, email, role);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtTokenProvider.validateToken(token));

        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token));
        assertEquals(role.name(), jwtTokenProvider.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Should validate token successfully for valid token")
    void shouldValidateValidToken() {
        String token = jwtTokenProvider.generateToken(1L, "user@test.com", "COLLEGE_ADMIN");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date pastExpiry = new Date(now.getTime() - 1000); // 1 second in the past

        String expiredToken = Jwts.builder()
                .subject("expired@test.com")
                .claim("userId", 10L)
                .claim("role", "TEAM_CAPTAIN")
                .issuedAt(new Date(now.getTime() - 2000))
                .expiration(pastExpiry)
                .signWith(key)
                .compact();

        assertFalse(jwtTokenProvider.validateToken(expiredToken));
    }

    @Test
    @DisplayName("Should reject token with invalid signature")
    void shouldRejectInvalidSignatureToken() {
        String differentSecret = "different-secret-key-that-is-at-least-256-bits-long-for-testing-bad-signature";
        SecretKey otherKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));

        String tokenFromOtherKey = Jwts.builder()
                .subject("hacker@test.com")
                .claim("userId", 99L)
                .claim("role", "PLATFORM_ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(otherKey)
                .compact();

        assertFalse(jwtTokenProvider.validateToken(tokenFromOtherKey));
    }

    @Test
    @DisplayName("Should reject malformed token string")
    void shouldRejectMalformedToken() {
        assertFalse(jwtTokenProvider.validateToken("not.a.valid.jwt.token"));
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    @DisplayName("Should fail initialization if secret is missing or empty")
    void shouldFailIfSecretMissing() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "");
        assertThrows(IllegalStateException.class, provider::init);

        ReflectionTestUtils.setField(provider, "jwtSecret", "   ");
        assertThrows(IllegalStateException.class, provider::init);
    }

    @Test
    @DisplayName("Should fail initialization if secret is shorter than 256 bits")
    void shouldFailIfSecretTooShort() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "short-secret-less-than-32-bytes");
        assertThrows(IllegalStateException.class, provider::init);
    }
}
