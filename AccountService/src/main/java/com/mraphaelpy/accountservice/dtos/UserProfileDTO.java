package com.mraphaelpy.accountservice.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class UserProfileDTO {
    private UUID id;
    private UUID userId;
    private String name;
    private String cpf;
    private String email;
    private LocalDate dataNascimento;
}
