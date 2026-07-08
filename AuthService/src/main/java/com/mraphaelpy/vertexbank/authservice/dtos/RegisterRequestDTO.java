package com.mraphaelpy.vertexbank.authservice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO{

    @NotBlank(message = "Email is required")
    @Email(message = "The email address is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 64 characters")
    private String password;
}
