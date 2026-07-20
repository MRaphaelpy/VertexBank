package com.mraphaelpy.accountservice.services;

import com.mraphaelpy.accountservice.dtos.AccountResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountQueryService {
    BigDecimal getBalance(UUID accountId);
    AccountResponse getAccountById(UUID accountId);
    AccountResponse getAccountByUserId(UUID userId);
    AccountResponse getAccountByAccountNumber(String accountNumber);
    AccountResponse getAccountByAgencyAndAccountNumber(String agency, String accountNumber);
}
