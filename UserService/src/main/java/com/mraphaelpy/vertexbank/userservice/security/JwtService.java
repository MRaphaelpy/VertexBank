package com.mraphaelpy.vertexbank.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return Jwts.claims().build();
        }
    }

    public boolean isClaimsNotExpired(Claims claims) {
        try {
            return claims != null && claims.getExpiration() != null
                    && !claims.getExpiration().toInstant().isBefore(Instant.now());
        } catch (Exception ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
        Claims claims = extractClaims(token);
        return !claims.isEmpty() ? claims.getSubject() : null;
    }

    public UUID extractUserId(String token) {
        Claims claims = extractClaims(token);
        if (claims.isEmpty()) {
            return null;
        }
        try {
            String userId = claims.get("userId", String.class);
            return userId != null ? UUID.fromString(userId) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractClaims(token);
        if (claims.isEmpty()) {
            return List.of();
        }
        List<String> roles = claims.get("roles", List.class);
        return roles != null ? roles : List.of();
    }

    public boolean isTokenValid(String token) {
        Claims claims = extractClaims(token);
        return !claims.isEmpty() && isClaimsNotExpired(claims);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractClaims(token);
        return !claims.isEmpty() ? claimsResolver.apply(claims) : null;
    }

    private SecretKey getSignInKey() {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid security.jwt.secret-key: expected Base64-encoded key material", ex);
        }
    }
}
