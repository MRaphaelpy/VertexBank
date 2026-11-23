package com.mraphaelpy.accountservice.dtos;

import com.mraphaelpy.accountservice.dtos.UpdateBalanceRequest.OperationType;
import com.mraphaelpy.accountservice.entity.Account;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class OperationTypeTest {

    @Test
    void depositDelegatesToAccountDeposit() {
        Account account = mock(Account.class);
        BigDecimal amount = new BigDecimal("25.00");

        OperationType.DEPOSIT.execute(account, amount);

        verify(account).deposit(amount);
        verifyNoMoreInteractions(account);
    }

    @Test
    void withdrawDelegatesToAccountWithdraw() {
        Account account = mock(Account.class);
        BigDecimal amount = new BigDecimal("25.00");

        OperationType.WITHDRAW.execute(account, amount);

        verify(account).withdraw(amount);
        verifyNoMoreInteractions(account);
    }
}
