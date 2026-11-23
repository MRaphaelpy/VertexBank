package com.mraphaelpy.accountservice.services;

import com.mraphaelpy.accountservice.client.UserClient;
import com.mraphaelpy.accountservice.dtos.AccountResponse;
import com.mraphaelpy.accountservice.dtos.UpdateBalanceRequest.OperationType;
import com.mraphaelpy.accountservice.dtos.UserProfileDTO;
import com.mraphaelpy.accountservice.entity.Account;
import com.mraphaelpy.accountservice.entity.AccountStatus;
import com.mraphaelpy.accountservice.exception.AccountNotFoundException;
import com.mraphaelpy.accountservice.exception.NoBalanceException;
import com.mraphaelpy.accountservice.exception.UserNotFoundException;
import com.mraphaelpy.accountservice.generator.AccountNumberGenerator;
import com.mraphaelpy.accountservice.mappers.AccountMapper;
import com.mraphaelpy.accountservice.repository.AccountRepository;
import com.mraphaelpy.accountservice.utils.ExceptionMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountCommandServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @Mock
    private UserClient userClient;

    private final AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    private AccountCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AccountCommandServiceImpl(accountRepository, accountMapper, accountNumberGenerator, userClient);
    }

    @Test
    void createAccountValidatesUserAndPersistsNewAccount() {
        UUID userId = UUID.randomUUID();
        when(userClient.getUserById(userId)).thenReturn(new UserProfileDTO());
        when(accountNumberGenerator.generateAgency()).thenReturn("0001");
        when(accountNumberGenerator.generateAccountNumber()).thenReturn("1234567890");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = service.createAccount(userId);

        ArgumentCaptor<Account> saved = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(saved.capture());
        assertThat(saved.getValue().getUserId()).isEqualTo(userId);
        assertThat(saved.getValue().getAgency()).isEqualTo("0001");
        assertThat(saved.getValue().getAccountNumber()).isEqualTo("1234567890");

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getAgency()).isEqualTo("0001");
        assertThat(response.getAccountNumber()).isEqualTo("1234567890");
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void createAccountWithNullUserIdThrowsWithoutCallingUserService() {
        assertThatThrownBy(() -> service.createAccount(null))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(ExceptionMessages.USER_NOT_FOUND);

        verifyNoInteractions(userClient, accountRepository, accountNumberGenerator);
    }

    @Test
    void createAccountPropagatesUserNotFoundFromUserService() {
        UUID userId = UUID.randomUUID();
        when(userClient.getUserById(userId)).thenThrow(new UserNotFoundException(ExceptionMessages.USER_NOT_FOUND));

        assertThatThrownBy(() -> service.createAccount(userId))
                .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(accountRepository, accountNumberGenerator);
    }

    @Test
    void depositUpdatesAndPersistsBalance() {
        Account account = accountWithBalance("100.00");
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        AccountResponse response = service.updateBalance(account.getId(), new BigDecimal("50.00"), OperationType.DEPOSIT);

        assertThat(response.getBalance()).isEqualByComparingTo("150.00");
        verify(accountRepository).save(account);
    }

    @Test
    void withdrawUpdatesAndPersistsBalance() {
        Account account = accountWithBalance("100.00");
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        AccountResponse response = service.updateBalance(account.getId(), new BigDecimal("40.00"), OperationType.WITHDRAW);

        assertThat(response.getBalance()).isEqualByComparingTo("60.00");
    }

    @Test
    void withdrawWithInsufficientBalanceDoesNotPersist() {
        Account account = accountWithBalance("10.00");
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.updateBalance(account.getId(), new BigDecimal("10.01"), OperationType.WITHDRAW))
                .isInstanceOf(NoBalanceException.class);

        verify(accountRepository, never()).save(any());
        assertThat(account.getBalance()).isEqualByComparingTo("10.00");
    }

    @Test
    void updateBalanceWithNullTypeThrowsBeforeLoadingAccount() {
        UUID accountId = UUID.randomUUID();

        assertThatThrownBy(() -> service.updateBalance(accountId, BigDecimal.TEN, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ExceptionMessages.INVALID_OPERATION);

        verifyNoInteractions(accountRepository);
    }

    @Test
    void updateBalanceOfUnknownAccountThrowsNotFound() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateBalance(accountId, BigDecimal.TEN, OperationType.DEPOSIT))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage(ExceptionMessages.ACCOUNT_NOT_FOUND);
    }

    @Test
    void updateBalanceWithNullAccountIdThrowsNotFound() {
        assertThatThrownBy(() -> service.updateBalance(null, BigDecimal.TEN, OperationType.DEPOSIT))
                .isInstanceOf(AccountNotFoundException.class);

        verifyNoInteractions(accountRepository);
    }

    @Test
    void deactivateAccountTerminatesAndPersists() {
        Account account = accountWithBalance("0.00");
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        service.deactivateAccount(account.getId());

        assertThat(account.getStatus()).isEqualTo(AccountStatus.TERMINATED);
        verify(accountRepository).save(account);
    }

    @Test
    void reactivateAccountActivatesAndPersists() {
        Account account = accountWithBalance("0.00");
        account.deactivate();
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        service.reactivateAccount(account.getId());

        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(accountRepository).save(account);
    }

    @Test
    void deactivateUnknownAccountThrowsNotFound() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivateAccount(accountId))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void reactivateUnknownAccountThrowsNotFound() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reactivateAccount(accountId))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository, never()).save(any());
    }

    private Account accountWithBalance(String balance) {
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setUserId(UUID.randomUUID());
        account.setAgency("0001");
        account.setAccountNumber("1234567890");
        account.setBalance(new BigDecimal(balance));
        return account;
    }
}
