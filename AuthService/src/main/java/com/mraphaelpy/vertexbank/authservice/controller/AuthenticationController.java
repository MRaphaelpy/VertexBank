package com.mraphaelpy.vertexbank.authservice.controller;

import com.mraphaelpy.vertexbank.authservice.dtos.LoginRequestDTO;
import com.mraphaelpy.vertexbank.authservice.dtos.LoginResponse;
import com.mraphaelpy.vertexbank.authservice.dtos.RefreshTokenRequest;
import com.mraphaelpy.vertexbank.authservice.dtos.RefreshTokenResponse;
import com.mraphaelpy.vertexbank.authservice.dtos.RegisterRequestDTO;
import com.mraphaelpy.vertexbank.authservice.dtos.RegisterResponse;
import com.mraphaelpy.vertexbank.authservice.dtos.TokenValidationResponse;
import com.mraphaelpy.vertexbank.authservice.service.AuthenticationService;
import com.mraphaelpy.vertexbank.authservice.service.JwtService;
import com.mraphaelpy.vertexbank.authservice.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationController(JwtService jwtService,
                                    AuthenticationService authenticationService,
                                    RefreshTokenService refreshTokenService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/signup")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        return ResponseEntity.status(201).body(authenticationService.signup(registerRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        return ResponseEntity.ok(authenticationService.authenticate(loginRequestDTO));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.refreshAccessToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestParam String token) {
        return ResponseEntity.ok(jwtService.validateToken(token));
    }
}
