package com.mraphaelpy.accountservice.services;

import com.mraphaelpy.accountservice.dtos.AccountResponse;
import com.mraphaelpy.accountservice.dtos.UpdateBalanceRequest;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountCommandService {
    AccountResponse createAccount(UUID userId);
    AccountResponse updateBalance(UUID accountId, BigDecimal amount, UpdateBalanceRequest.OperationType type);
    void deactivateAccount(UUID accountId);
    void reactivateAccount(UUID accountId);
}
