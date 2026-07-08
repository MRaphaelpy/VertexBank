package com.mraphaelpy.vertexbank.authservice.controller;

import com.mraphaelpy.vertexbank.authservice.dtos.LoginRequestDTO;
import com.mraphaelpy.vertexbank.authservice.dtos.LoginResponse;
import com.mraphaelpy.vertexbank.authservice.dtos.RegisterRequestDTO;
import com.mraphaelpy.vertexbank.authservice.entity.UserCredential;
import com.mraphaelpy.vertexbank.authservice.service.AuthenticationService;
import com.mraphaelpy.vertexbank.authservice.service.JwtService;
import com.mraphaelpy.vertexbank.authservice.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;

    public AuthenticationController(JwtService jwtService,
                                    AuthenticationService authenticationService,
                                    RefreshTokenService refreshTokenService,
                                    UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserCredential> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        UserCredential registered = authenticationService.signup(registerRequestDTO);
        return ResponseEntity.ok(registered);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        return ResponseEntity.ok(authenticationService.authenticate(loginRequestDTO));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(
                Map.of("token",
                        refreshTokenService.refreshAccessToken(payload.get("refreshToken")))
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody Map<String, String> payload) {
        String refreshToken = payload.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body("Refresh token is required.");
        }
        refreshTokenService.logout(refreshToken);
        return ResponseEntity.ok("Logged out successfully.");
    }

    /**
     * Endpoint interno para o API Gateway validar tokens JWT.
     * Retorna email, userId e roles do token se for válido.
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            String email = jwtService.extractUsername(token);
            var userDetails = userDetailsService.loadUserByUsername(email);
            if (!jwtService.isTokenValid(token, userDetails)) {
                return ResponseEntity.status(401).body(Map.of("valid", false, "error", "Token inválido ou expirado"));
            }
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "email", email,
                    "userId", jwtService.extractUserId(token),
                    "roles", jwtService.extractRoles(token)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "error", e.getMessage()));
        }
    }
}
