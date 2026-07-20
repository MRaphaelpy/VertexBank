package com.mraphaelpy.accountservice.services;

import com.mraphaelpy.accountservice.client.UserClient;
import com.mraphaelpy.accountservice.dtos.AccountResponse;
import com.mraphaelpy.accountservice.dtos.UpdateBalanceRequest;
import com.mraphaelpy.accountservice.entity.Account;
import com.mraphaelpy.accountservice.exception.AccountNotFoundException;
import com.mraphaelpy.accountservice.exception.UserNotFoundException;
import com.mraphaelpy.accountservice.generator.AccountNumberGenerator;
import com.mraphaelpy.accountservice.mappers.AccountMapper;
import com.mraphaelpy.accountservice.repository.AccountRepository;
import com.mraphaelpy.accountservice.utils.ExceptionMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountCommandServiceImpl implements AccountCommandService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AccountNumberGenerator accountNumberGenerator;
    private final UserClient userClient;

    @Override
    public AccountResponse createAccount(UUID userId) {
        Optional.ofNullable(userId)
                .map(userClient::getUserById)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessages.USER_NOT_FOUND));

        Account account = new Account();
        account.setUserId(userId);
        account.setAgency(accountNumberGenerator.generateAgency());
        account.setAccountNumber(accountNumberGenerator.generateAccountNumber());
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    public AccountResponse updateBalance(UUID accountId, BigDecimal amount, UpdateBalanceRequest.OperationType type) {
        UpdateBalanceRequest.OperationType operationType = Optional.ofNullable(type)
                .orElseThrow(() -> new IllegalArgumentException(ExceptionMessages.INVALID_OPERATION));

        Account account = Optional.ofNullable(accountId)
                .flatMap(accountRepository::findById)
                .orElseThrow(() -> new AccountNotFoundException(ExceptionMessages.ACCOUNT_NOT_FOUND));

        operationType.execute(account, amount);

        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    public void deactivateAccount(UUID accountId) {
        Account account = Optional.ofNullable(accountId)
                .flatMap(accountRepository::findById)
                .orElseThrow(() -> new AccountNotFoundException(ExceptionMessages.ACCOUNT_NOT_FOUND));
        account.deactivate();
        accountRepository.save(account);
    }

    @Override
    public void reactivateAccount(UUID accountId) {
        Account account = Optional.ofNullable(accountId)
                .flatMap(accountRepository::findById)
                .orElseThrow(() -> new AccountNotFoundException(ExceptionMessages.ACCOUNT_NOT_FOUND));
        account.reactivate();
        accountRepository.save(account);
    }
}
