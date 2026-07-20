package com.mraphaelpy.accountservice.generator;

import com.mraphaelpy.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class RandomAccountNumberGenerator implements AccountNumberGenerator {

    private final AccountRepository accountRepository;

    @Override
    public String generateAgency() {
        return String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    @Override
    public String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.format("%010d", ThreadLocalRandom.current().nextLong(10_000_000_000L));
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
