package com.mraphaelpy.vertexbank.userservice.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateUserProfileRequest {

    @NotBlank
    private String name;

    @NotBlank
    @CPF
    private String cpf;

    @NotBlank
    @Email
    private String email;

    @NotNull
    @DateTimeFormat
    private LocalDate dataNascimento;

    @NotNull
    @Valid
    private AddressDTO address;

    @NotNull
    @Valid
    private List<PhoneDTO> phones = new ArrayList<>();
}
