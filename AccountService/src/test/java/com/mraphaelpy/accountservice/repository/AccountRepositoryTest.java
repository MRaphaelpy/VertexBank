package com.mraphaelpy.accountservice.repository;

import com.mraphaelpy.accountservice.entity.Account;
import com.mraphaelpy.accountservice.entity.AccountStatus;
import com.mraphaelpy.accountservice.support.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void savePersistsDefaultsAndTimestamps() {
        Account saved = accountRepository.saveAndFlush(newAccount(UUID.randomUUID(), "0001", "1000000001"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByUserIdReturnsAccount() {
        UUID userId = UUID.randomUUID();
        Account saved = accountRepository.saveAndFlush(newAccount(userId, "0001", "1000000002"));

        assertThat(accountRepository.findByUserId(userId)).get()
                .extracting(Account::getId).isEqualTo(saved.getId());
        assertThat(accountRepository.findByUserId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findByAccountNumberAndExistsByAccountNumber() {
        accountRepository.saveAndFlush(newAccount(UUID.randomUUID(), "0001", "1000000003"));

        assertThat(accountRepository.findByAccountNumber("1000000003")).isPresent();
        assertThat(accountRepository.existsByAccountNumber("1000000003")).isTrue();
        assertThat(accountRepository.existsByAccountNumber("9999999999")).isFalse();
    }

    @Test
    void findByAgencyAndAccountNumberRequiresBothToMatch() {
        accountRepository.saveAndFlush(newAccount(UUID.randomUUID(), "0001", "1000000004"));

        assertThat(accountRepository.findByAgencyAndAccountNumber("0001", "1000000004")).isPresent();
        assertThat(accountRepository.findByAgencyAndAccountNumber("0002", "1000000004")).isEmpty();
    }

    @Test
    void balanceKeepsDecimalPrecision() {
        Account account = newAccount(UUID.randomUUID(), "0001", "1000000005");
        account.deposit(new BigDecimal("1234.56"));
        Account saved = accountRepository.saveAndFlush(account);

        assertThat(accountRepository.findById(saved.getId())).get()
                .extracting(Account::getBalance)
                .satisfies(balance -> assertThat(balance).isEqualByComparingTo("1234.56"));
    }

    @Test
    void userCannotHaveTwoAccounts() {
        UUID userId = UUID.randomUUID();
        accountRepository.saveAndFlush(newAccount(userId, "0001", "1000000006"));

        assertThatThrownBy(() -> accountRepository.saveAndFlush(newAccount(userId, "0001", "1000000007")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void accountNumberMustBeUnique() {
        accountRepository.saveAndFlush(newAccount(UUID.randomUUID(), "0001", "1000000008"));

        assertThatThrownBy(() -> accountRepository.saveAndFlush(newAccount(UUID.randomUUID(), "0002", "1000000008")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Account newAccount(UUID userId, String agency, String accountNumber) {
        Account account = new Account();
        account.setUserId(userId);
        account.setAgency(agency);
        account.setAccountNumber(accountNumber);
        return account;
    }
}
