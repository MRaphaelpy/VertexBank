package com.mraphaelpy.accountservice.controller;

import com.mraphaelpy.accountservice.config.SecurityConfiguration;
import com.mraphaelpy.accountservice.dtos.AccountResponse;
import com.mraphaelpy.accountservice.dtos.UpdateBalanceRequest.OperationType;
import com.mraphaelpy.accountservice.entity.AccountStatus;
import com.mraphaelpy.accountservice.exception.AccountNotFoundException;
import com.mraphaelpy.accountservice.exception.NoBalanceException;
import com.mraphaelpy.accountservice.exception.UserNotFoundException;
import com.mraphaelpy.accountservice.security.JwtAuthenticationEntryPoint;
import com.mraphaelpy.accountservice.security.JwtAuthenticationFilter;
import com.mraphaelpy.accountservice.security.JwtService;
import com.mraphaelpy.accountservice.services.AccountCommandService;
import com.mraphaelpy.accountservice.services.AccountQueryService;
import com.mraphaelpy.accountservice.support.JwtTestTokens;
import com.mraphaelpy.accountservice.utils.ExceptionMessages;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, JwtService.class})
@TestPropertySource(properties = JwtTestTokens.SECRET_PROPERTY)
class AccountControllerTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountCommandService accountCommandService;

    @MockitoBean
    private AccountQueryService accountQueryService;

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/accounts/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(accountQueryService, accountCommandService);
    }

    @Test
    void requestWithExpiredTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/accounts/me")
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.expiredBearer(userId)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(accountQueryService);
    }

    @Test
    void requestWithTokenSignedByAnotherKeyIsUnauthorized() throws Exception {
        mockMvc.perform(get("/accounts/me")
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearerSignedWithOtherKey(userId)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(accountQueryService);
    }

    @Test
    void createAccountUsesUserIdFromToken() throws Exception {
        when(accountCommandService.createAccount(userId)).thenReturn(response("0.00"));

        mockMvc.perform(post("/accounts")
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearer(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(accountCommandService).createAccount(userId);
    }

    @Test
    void createAccountForUnknownUserReturnsNotFound() throws Exception {
        when(accountCommandService.createAccount(userId))
                .thenThrow(new UserNotFoundException(ExceptionMessages.USER_NOT_FOUND));

        mockMvc.perform(post("/accounts")
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearer(userId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ExceptionMessages.USER_NOT_FOUND));
    }

    @Test
    void getMyAccountReturnsAccountOfTokenUser() throws Exception {
        when(accountQueryService.getAccountByUserId(userId)).thenReturn(response("150.00"));

        mockMvc.perform(get("/accounts/me")
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearer(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.00));
    }

    @Test
    void getUnknownAccountReturnsNotFound() throws Exception {
        when(accountQueryService.getAccountById(accountId))
                .thenThrow(new AccountNotFoundException(ExceptionMessages.ACCOUNT_NOT_FOUND));

        mockMvc.perform(get("/accounts/{id}", accountId)
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearer(userId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(ExceptionMessages.ACCOUNT_NOT_FOUND));
    }

    @Test
    void getBalanceReturnsValue() throws Exception {
        when(accountQueryService.getBalance(accountId)).thenReturn(new BigDecimal("42.50"));

        mockMvc.perform(get("/accounts/{id}/balance", accountId)
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearer(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(42.50));
    }

    @Test
    void depositReturnsUpdatedAccount() throws Exception {
        when(accountCommandService.updateBalance(accountId, new BigDecimal("50.00"), OperationType.DEPOSIT))
                .thenReturn(response("50.00"));

        mockMvc.perform(put("/accounts/{id}/balance", accountId)
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "DEPOSIT", "amount": 50.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(50.00));
    }

    @Test
    void withdrawWithInsufficientBalanceReturnsBadRequest() throws Exception {
        when(accountCommandService.updateBalance(eq(accountId), any(), eq(OperationType.WITHDRAW)))
                .thenThrow(new NoBalanceException(ExceptionMessages.INSUFFICIENT_BALANCE));

        mockMvc.perform(put("/accounts/{id}/balance", accountId)
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "WITHDRAW", "amount": 1000.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ExceptionMessages.INSUFFICIENT_BALANCE));
    }

    @Test
    void operationOnInactiveAccountReturnsBadRequest() throws Exception {
        when(accountCommandService.updateBalance(eq(accountId), any(), eq(OperationType.DEPOSIT)))
                .thenThrow(new IllegalStateException(ExceptionMessages.ACCOUNT_INACTIVE));

        mockMvc.perform(put("/accounts/{id}/balance", accountId)
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "DEPOSIT", "amount": 10.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ExceptionMessages.ACCOUNT_INACTIVE));
    }

    @Test
    void updateBalanceWithZeroAmountIsRejectedByValidation() throws Exception {
        mockMvc.perform(put("/accounts/{id}/balance", accountId)
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "DEPOSIT", "amount": 0}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountCommandService);
    }

    @Test
    void updateBalanceWithoutTypeIsRejectedByValidation() throws Exception {
        mockMvc.perform(put("/accounts/{id}/balance", accountId)
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 10.00}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountCommandService);
    }

    @Test
    void deactivateAccountReturnsNoContent() throws Exception {
        mockMvc.perform(put("/accounts/{id}/deactivate", accountId)
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearer(userId)))
                .andExpect(status().isNoContent());

        verify(accountCommandService).deactivateAccount(accountId);
    }

    @Test
    void reactivateAccountReturnsNoContent() throws Exception {
        mockMvc.perform(put("/accounts/{id}/reactivate", accountId)
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearer(userId)))
                .andExpect(status().isNoContent());

        verify(accountCommandService).reactivateAccount(accountId);
    }

    private AccountResponse response(String balance) {
        AccountResponse response = new AccountResponse();
        response.setId(accountId);
        response.setUserId(userId);
        response.setAgency("0001");
        response.setAccountNumber("1234567890");
        response.setBalance(new BigDecimal(balance));
        response.setStatus(AccountStatus.ACTIVE);
        return response;
    }
}
