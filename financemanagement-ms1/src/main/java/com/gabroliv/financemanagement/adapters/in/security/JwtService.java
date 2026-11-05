package com.gabroliv.financemanagement.adapters.in.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(@Value("${security.jwt.secret}") String secret, @Value("${security.jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    public String generateToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", "USER")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey).compact();
    }

    public AuthenticatedUserDetails getAuthenticatedUser(String token) {
        Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();

        Long userId = Long.parseLong(claims.getSubject());
        String username = claims.get("username", String.class);

        return new AuthenticatedUserDetails(userId, username, "");
    }
}
