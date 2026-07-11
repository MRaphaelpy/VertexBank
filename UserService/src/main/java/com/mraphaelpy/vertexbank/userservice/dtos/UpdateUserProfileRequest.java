package com.mraphaelpy.vertexbank.userservice.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UpdateUserProfileRequest {

    private String name;

    @Email
    private String email;

    private LocalDate dataNascimento;

    @Valid
    private AddressDTO address;

    @Valid
    private List<PhoneDTO> phones;
}
