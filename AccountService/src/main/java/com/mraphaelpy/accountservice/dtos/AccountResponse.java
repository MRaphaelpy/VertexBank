package com.mraphaelpy.accountservice.dtos;

import com.mraphaelpy.accountservice.entity.AccountStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class AccountResponse {
    private UUID id;
    private UUID userId;
    private String accountNumber;
    private String agency;
    private BigDecimal balance;
    private AccountStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
