package com.mraphaelpy.accountservice.entity;

import com.mraphaelpy.accountservice.exception.NoBalanceException;
import com.mraphaelpy.accountservice.utils.ExceptionMessages;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 10)
    private String accountNumber;

    @Column(nullable = false, length = 4)
    private String agency;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public void deposit(BigDecimal amount) {
        validateAmount(amount);
        validateActive();
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        validateAmount(amount);
        validateActive();
        if (this.balance.compareTo(amount) < 0) {
            throw new NoBalanceException(ExceptionMessages.INSUFFICIENT_BALANCE);
        }
        this.balance = this.balance.subtract(amount);
    }

    public void validateActive() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new IllegalStateException(ExceptionMessages.ACCOUNT_INACTIVE);
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(ExceptionMessages.INVALID_AMOUNT);
        }
    }

    public void deactivate() {
        this.status = AccountStatus.TERMINATED;
    }

    public void reactivate() {
        this.status = AccountStatus.ACTIVE;
    }
}

