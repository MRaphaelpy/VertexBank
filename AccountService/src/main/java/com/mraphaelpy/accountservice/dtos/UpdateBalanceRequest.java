package com.mraphaelpy.accountservice.dtos;

import com.mraphaelpy.accountservice.entity.Account;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateBalanceRequest {

    public enum OperationType {
        DEPOSIT {
            @Override
            public void execute(Account account, BigDecimal amount) {
                account.deposit(amount);
            }
        },
        WITHDRAW {
            @Override
            public void execute(Account account, BigDecimal amount) {
                account.withdraw(amount);
            }
        };

        public abstract void execute(Account account, BigDecimal amount);
    }

    @NotNull(message = "O tipo de operação é obrigatório")
    private OperationType type;

    @NotNull(message = "O valor é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    private BigDecimal amount;
}
