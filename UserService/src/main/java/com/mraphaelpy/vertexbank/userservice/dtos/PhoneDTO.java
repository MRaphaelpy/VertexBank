package com.mraphaelpy.vertexbank.userservice.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneDTO {
    @NotBlank
    private String numero;
}