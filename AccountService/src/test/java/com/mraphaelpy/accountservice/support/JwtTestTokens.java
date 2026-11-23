package com.mraphaelpy.accountservice.support;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class JwtTestTokens {

    public static final String SECRET =
            "T17h6qB7hY00dzNkb2b0X7gcPAiWfoeuC1KYRQpB51aTSD0ev42rUob41NCWD1rUHKekpRtY5cmviFe8YdQ03w==";

    public static final String SECRET_PROPERTY = "security.jwt.secret-key=" + SECRET;

    private JwtTestTokens() {
    }

    public static String bearer(UUID userId) {
        return "Bearer " + token(userId, Instant.now().plus(Duration.ofHours(1)), key(SECRET));
    }

    public static String expiredBearer(UUID userId) {
        return "Bearer " + token(userId, Instant.now().minus(Duration.ofMinutes(1)), key(SECRET));
    }

    public static String bearerSignedWithOtherKey(UUID userId) {
        SecretKey otherKey = Jwts.SIG.HS512.key().build();
        return "Bearer " + token(userId, Instant.now().plus(Duration.ofHours(1)), otherKey);
    }

    private static String token(UUID userId, Instant expiration, SecretKey key) {
        return Jwts.builder()
                .subject("cliente@vertexbank.com")
                .claim("userId", userId.toString())
                .claim("roles", List.of("ROLE_USER"))
                .issuedAt(Date.from(Instant.now().minus(Duration.ofHours(2))))
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

    private static SecretKey key(String base64Secret) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }
}
