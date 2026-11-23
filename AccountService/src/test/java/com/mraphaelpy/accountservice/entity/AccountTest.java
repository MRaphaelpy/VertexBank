package com.mraphaelpy.accountservice.entity;

import com.mraphaelpy.accountservice.exception.NoBalanceException;
import com.mraphaelpy.accountservice.utils.ExceptionMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
    }

    @Test
    void newAccountStartsActiveWithZeroBalance() {
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void depositIncreasesBalance() {
        account.deposit(new BigDecimal("100.50"));
        account.deposit(new BigDecimal("0.50"));

        assertThat(account.getBalance()).isEqualByComparingTo("101.00");
    }

    @Test
    void withdrawDecreasesBalance() {
        account.deposit(new BigDecimal("100.00"));

        account.withdraw(new BigDecimal("30.25"));

        assertThat(account.getBalance()).isEqualByComparingTo("69.75");
    }

    @Test
    void withdrawOfEntireBalanceLeavesZero() {
        account.deposit(new BigDecimal("50.00"));

        account.withdraw(new BigDecimal("50.00"));

        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void withdrawGreaterThanBalanceThrowsAndKeepsBalance() {
        account.deposit(new BigDecimal("10.00"));

        assertThatThrownBy(() -> account.withdraw(new BigDecimal("10.01")))
                .isInstanceOf(NoBalanceException.class)
                .hasMessage(ExceptionMessages.INSUFFICIENT_BALANCE);
        assertThat(account.getBalance()).isEqualByComparingTo("10.00");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "0.00", "-0.01", "-100"})
    void depositRejectsNonPositiveAmounts(BigDecimal amount) {
        assertThatThrownBy(() -> account.deposit(amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ExceptionMessages.INVALID_AMOUNT);
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "0.00", "-0.01", "-100"})
    void withdrawRejectsNonPositiveAmounts(BigDecimal amount) {
        account.deposit(new BigDecimal("100.00"));

        assertThatThrownBy(() -> account.withdraw(amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ExceptionMessages.INVALID_AMOUNT);
        assertThat(account.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    void deactivatedAccountRejectsDeposit() {
        account.deactivate();

        assertThat(account.getStatus()).isEqualTo(AccountStatus.TERMINATED);
        assertThatThrownBy(() -> account.deposit(BigDecimal.TEN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(ExceptionMessages.ACCOUNT_INACTIVE);
    }

    @Test
    void deactivatedAccountRejectsWithdraw() {
        account.deposit(BigDecimal.TEN);
        account.deactivate();

        assertThatThrownBy(() -> account.withdraw(BigDecimal.ONE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(ExceptionMessages.ACCOUNT_INACTIVE);
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void blockedAccountRejectsOperations() {
        account.setStatus(AccountStatus.BLOCKED);

        assertThatThrownBy(() -> account.deposit(BigDecimal.TEN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(ExceptionMessages.ACCOUNT_INACTIVE);
    }

    @Test
    void reactivatedAccountAcceptsOperationsAgain() {
        account.deactivate();
        account.reactivate();

        account.deposit(BigDecimal.TEN);

        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.TEN);
    }
}
