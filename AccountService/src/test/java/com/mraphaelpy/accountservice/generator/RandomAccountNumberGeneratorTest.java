package com.mraphaelpy.accountservice.generator;

import com.mraphaelpy.accountservice.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RandomAccountNumberGeneratorTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private RandomAccountNumberGenerator generator;

    @Test
    void agencyHasFourDigits() {
        IntStream.range(0, 200).forEach(i ->
                assertThat(generator.generateAgency()).matches("\\d{4}"));
    }

    @Test
    void accountNumberHasTenDigits() {
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);

        IntStream.range(0, 200).forEach(i ->
                assertThat(generator.generateAccountNumber()).matches("\\d{10}"));
    }

    @Test
    void regeneratesWhileAccountNumberAlreadyExists() {
        when(accountRepository.existsByAccountNumber(anyString()))
                .thenReturn(true, true, false);

        String accountNumber = generator.generateAccountNumber();

        assertThat(accountNumber).matches("\\d{10}");
        verify(accountRepository, times(3)).existsByAccountNumber(anyString());
    }
}
