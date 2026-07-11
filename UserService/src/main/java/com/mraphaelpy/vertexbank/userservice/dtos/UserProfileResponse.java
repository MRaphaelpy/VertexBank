package com.mraphaelpy.vertexbank.userservice.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserProfileResponse {

    private UUID id;

    private UUID userId;

    private String name;

    private String cpf;

    private String email;

    private LocalDate dataNascimento;

    private AddressDTO address;

    private List<PhoneDTO> phones;
}
