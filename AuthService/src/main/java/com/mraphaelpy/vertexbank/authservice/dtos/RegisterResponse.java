package com.mraphaelpy.vertexbank.authservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RegisterResponse {
    private UUID id;
    private UUID userId;
    private String email;
}
