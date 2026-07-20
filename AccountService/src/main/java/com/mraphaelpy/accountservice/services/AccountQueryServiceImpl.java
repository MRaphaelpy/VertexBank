package com.mraphaelpy.accountservice.services;

import com.mraphaelpy.accountservice.dtos.AccountResponse;
import com.mraphaelpy.accountservice.entity.Account;
import com.mraphaelpy.accountservice.exception.AccountNotFoundException;
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
@Transactional(readOnly = true)
public class AccountQueryServiceImpl implements AccountQueryService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    public BigDecimal getBalance(UUID accountId) {
        Account account = Optional.ofNullable(accountId)
                .flatMap(accountRepository::findById)
                .orElseThrow(() -> new AccountNotFoundException(ExceptionMessages.ACCOUNT_NOT_FOUND));
        return account.getBalance();
    }

    @Override
    public AccountResponse getAccountById(UUID accountId) {
        Account account = Optional.ofNullable(accountId)
                .flatMap(accountRepository::findById)
                .orElseThrow(() -> new AccountNotFoundException(ExceptionMessages.ACCOUNT_NOT_FOUND));
        return accountMapper.toResponse(account);
    }

    @Override
    public AccountResponse getAccountByUserId(UUID userId) {
        Account account = Optional.ofNullable(userId)
                .flatMap(accountRepository::findByUserId)
                .orElseThrow(() -> new AccountNotFoundException(ExceptionMessages.ACCOUNT_NOT_FOUND));
        return accountMapper.toResponse(account);
    }

    @Override
    public AccountResponse getAccountByAccountNumber(String accountNumber) {
        Account account = Optional.ofNullable(accountNumber)
                .flatMap(accountRepository::findByAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(ExceptionMessages.ACCOUNT_NOT_FOUND));
        return accountMapper.toResponse(account);
    }

    @Override
    public AccountResponse getAccountByAgencyAndAccountNumber(String agency, String accountNumber) {
        if (agency == null || accountNumber == null) {
            throw new AccountNotFoundException(ExceptionMessages.ACCOUNT_NOT_FOUND);
        }
        Account account = accountRepository.findByAgencyAndAccountNumber(agency, accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(ExceptionMessages.ACCOUNT_NOT_FOUND));
        return accountMapper.toResponse(account);
    }
}
