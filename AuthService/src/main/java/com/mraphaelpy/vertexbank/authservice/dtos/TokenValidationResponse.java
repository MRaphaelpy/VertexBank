package com.mraphaelpy.vertexbank.authservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TokenValidationResponse {
    private boolean valid;
    private String email;
    private UUID userId;
    private List<String> roles;
}
