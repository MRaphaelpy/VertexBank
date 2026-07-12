package com.mraphaelpy.vertexbank.authservice.service;

import com.mraphaelpy.vertexbank.authservice.dtos.RefreshTokenResponse;
import com.mraphaelpy.vertexbank.authservice.entity.RefreshToken;
import com.mraphaelpy.vertexbank.authservice.repository.RefreshTokenRepository;
import com.mraphaelpy.vertexbank.authservice.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final JwtService jwtService;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserCredentialRepository userCredentialRepository,
            JwtService jwtService) {

        this.refreshTokenRepository = refreshTokenRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.jwtService = jwtService;
    }

    public RefreshToken createRefreshToken(UUID userID) {
        var token = new RefreshToken();
        token.setUserCredential(
                userCredentialRepository.findById(userID)
                        .orElseThrow(() -> new RuntimeException("User not found"))
        );
        token.setExpiresAt(Instant.now().plusMillis(refreshTokenDurationMs));
        token.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(token);
    }

    public RefreshToken findByToken(String requestToken) {
        RefreshToken token = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token."));

        if (isTokenExpired(token)) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired. Please login again.");
        }

        return token;
    }

    public RefreshTokenResponse refreshAccessToken(String requestToken) {
        RefreshToken refreshToken = findByToken(requestToken);
        String newToken = jwtService.generateToken(refreshToken.getUserCredential());
        return new RefreshTokenResponse(newToken);
    }

    public void logout(String requestToken) {
        RefreshToken token = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token."));

        refreshTokenRepository.delete(token);
    }

    private boolean isTokenExpired(RefreshToken token) {
        return token.getExpiresAt().isBefore(Instant.now());
    }
}
