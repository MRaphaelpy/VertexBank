package com.mraphaelpy.vertexbank.authservice.service;

import com.mraphaelpy.vertexbank.authservice.dtos.LoginRequestDTO;
import com.mraphaelpy.vertexbank.authservice.dtos.LoginResponse;
import com.mraphaelpy.vertexbank.authservice.dtos.RegisterResponse;
import com.mraphaelpy.vertexbank.authservice.entity.RefreshToken;
import com.mraphaelpy.vertexbank.authservice.entity.Role;
import com.mraphaelpy.vertexbank.authservice.entity.UserCredential;
import com.mraphaelpy.vertexbank.authservice.entity.UserRole;
import com.mraphaelpy.vertexbank.authservice.enums.RoleName;
import com.mraphaelpy.vertexbank.authservice.exceptions.EmailAlreadyInUseException;
import com.mraphaelpy.vertexbank.authservice.dtos.RegisterRequestDTO;
import com.mraphaelpy.vertexbank.authservice.repository.RoleRepository;
import com.mraphaelpy.vertexbank.authservice.repository.UserCredentialRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthenticationService {
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationService(UserCredentialRepository userCredentialRepository,
                                 PasswordEncoder passwordEncoder,
                                 AuthenticationManager authenticationManager,
                                 RoleRepository roleRepository,
                                 JwtService jwtService,
                                 RefreshTokenService refreshTokenService) {
        this.userCredentialRepository = userCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public RegisterResponse signup(RegisterRequestDTO input) {

        if (userCredentialRepository.existsByEmail(input.getEmail())) {
            throw new EmailAlreadyInUseException(input.getEmail());
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role USER not found. Run data seeder."));

        UserCredential userCredential = new UserCredential()
                .setUserId(UUID.randomUUID())
                .setEmail(input.getEmail())
                .setCreatedAt(Instant.now())
                .setPassword(passwordEncoder.encode(input.getPassword()));

        UserRole userRoleAssoc = new UserRole();
        userRoleAssoc.setUserCredential(userCredential);
        userRoleAssoc.setRole(userRole);
        userCredential.getRoles().add(userRoleAssoc);

        UserCredential saved = userCredentialRepository.save(userCredential);
        return new RegisterResponse(saved.getId(), saved.getUserId(), saved.getEmail());
    }

    public LoginResponse authenticate(LoginRequestDTO input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword())
        );
        UserCredential user = userCredentialRepository.findByEmail(input.getEmail())
                .orElseThrow();

        String jwtToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new LoginResponse()
                .setToken(jwtToken)
                .setExpiresIn(jwtService.getExpirationTime())
                .setRefreshToken(refreshToken.getToken());
    }
}
