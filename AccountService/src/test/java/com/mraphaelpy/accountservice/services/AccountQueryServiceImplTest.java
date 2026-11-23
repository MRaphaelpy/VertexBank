package com.mraphaelpy.accountservice.services;

import com.mraphaelpy.accountservice.dtos.AccountResponse;
import com.mraphaelpy.accountservice.entity.Account;
import com.mraphaelpy.accountservice.exception.AccountNotFoundException;
import com.mraphaelpy.accountservice.mappers.AccountMapper;
import com.mraphaelpy.accountservice.repository.AccountRepository;
import com.mraphaelpy.accountservice.utils.ExceptionMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountQueryServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    private final AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    private AccountQueryServiceImpl service;

    private Account account;

    @BeforeEach
    void setUp() {
        service = new AccountQueryServiceImpl(accountRepository, accountMapper);

        account = new Account();
        account.setId(UUID.randomUUID());
        account.setUserId(UUID.randomUUID());
        account.setAgency("0001");
        account.setAccountNumber("1234567890");
        account.setBalance(new BigDecimal("250.75"));
    }

    @Test
    void getBalanceReturnsAccountBalance() {
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        assertThat(service.getBalance(account.getId())).isEqualByComparingTo("250.75");
    }

    @Test
    void getBalanceOfUnknownAccountThrowsNotFound() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBalance(accountId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage(ExceptionMessages.ACCOUNT_NOT_FOUND);
    }

    @Test
    void getAccountByIdMapsAllFields() {
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        AccountResponse response = service.getAccountById(account.getId());

        assertThat(response.getId()).isEqualTo(account.getId());
        assertThat(response.getUserId()).isEqualTo(account.getUserId());
        assertThat(response.getAgency()).isEqualTo("0001");
        assertThat(response.getAccountNumber()).isEqualTo("1234567890");
        assertThat(response.getBalance()).isEqualByComparingTo("250.75");
        assertThat(response.getStatus()).isEqualTo(account.getStatus());
    }

    @Test
    void getAccountByUserIdReturnsAccount() {
        when(accountRepository.findByUserId(account.getUserId())).thenReturn(Optional.of(account));

        assertThat(service.getAccountByUserId(account.getUserId()).getId()).isEqualTo(account.getId());
    }

    @Test
    void getAccountByUserIdWithoutAccountThrowsNotFound() {
        UUID userId = UUID.randomUUID();
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAccountByUserId(userId))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void getAccountByAccountNumberReturnsAccount() {
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));

        assertThat(service.getAccountByAccountNumber("1234567890").getId()).isEqualTo(account.getId());
    }

    @Test
    void getAccountByAgencyAndAccountNumberReturnsAccount() {
        when(accountRepository.findByAgencyAndAccountNumber("0001", "1234567890")).thenReturn(Optional.of(account));

        assertThat(service.getAccountByAgencyAndAccountNumber("0001", "1234567890").getId())
                .isEqualTo(account.getId());
    }

    @Test
    void getAccountByAgencyAndAccountNumberWithNullArgumentsThrowsWithoutQuerying() {
        assertThatThrownBy(() -> service.getAccountByAgencyAndAccountNumber(null, "1234567890"))
                .isInstanceOf(AccountNotFoundException.class);
        assertThatThrownBy(() -> service.getAccountByAgencyAndAccountNumber("0001", null))
                .isInstanceOf(AccountNotFoundException.class);

        verifyNoInteractions(accountRepository);
    }

    @Test
    void nullIdentifiersThrowNotFoundWithoutQuerying() {
        assertThatThrownBy(() -> service.getBalance(null)).isInstanceOf(AccountNotFoundException.class);
        assertThatThrownBy(() -> service.getAccountById(null)).isInstanceOf(AccountNotFoundException.class);
        assertThatThrownBy(() -> service.getAccountByUserId(null)).isInstanceOf(AccountNotFoundException.class);
        assertThatThrownBy(() -> service.getAccountByAccountNumber(null)).isInstanceOf(AccountNotFoundException.class);

        verifyNoInteractions(accountRepository);
    }
}
