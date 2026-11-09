package com.gabroliv.financemanagement.adapters.in.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(
            "01234567890123456789012345678901"
                    .getBytes(StandardCharsets.UTF_8)
    );

    @Test
    void shouldThrowExceptionWhenTokenIsExpired() {
        JwtService jwtService = new JwtService(SECRET, -1L);

        String expiredToken = jwtService.generateToken(1L, "testuser");

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.getAuthenticatedUser(expiredToken)
        );
    }

    @Test
    void shouldThrowExceptionWhenTokenIsTampered() {
        JwtService jwtService = new JwtService(SECRET, 3_600_000L);

        String validToken = jwtService.generateToken(1L, "testuser");

        String tamperedToken = validToken.substring(0, validToken.length() - 1)
                + (validToken.endsWith("a") ? "b" : "a");

        assertThrows(
                JwtException.class,
                () -> jwtService.getAuthenticatedUser(tamperedToken)
        );
    }
}
