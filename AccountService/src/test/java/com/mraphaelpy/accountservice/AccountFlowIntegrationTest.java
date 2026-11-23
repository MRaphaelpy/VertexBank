package com.mraphaelpy.accountservice;

import com.mraphaelpy.accountservice.client.UserClient;
import com.mraphaelpy.accountservice.dtos.UserProfileDTO;
import com.mraphaelpy.accountservice.entity.Account;
import com.mraphaelpy.accountservice.entity.AccountStatus;
import com.mraphaelpy.accountservice.exception.UserNotFoundException;
import com.mraphaelpy.accountservice.repository.AccountRepository;
import com.mraphaelpy.accountservice.support.JwtTestTokens;
import com.mraphaelpy.accountservice.support.TestcontainersConfiguration;
import com.mraphaelpy.accountservice.utils.ExceptionMessages;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = JwtTestTokens.SECRET_PROPERTY)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AccountFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockitoBean
    private UserClient userClient;

    @Test
    void fullAccountLifecyclePersistsBalanceInDatabase() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = JwtTestTokens.bearer(userId);
        when(userClient.getUserById(userId)).thenReturn(new UserProfileDTO());

        String created = mockMvc.perform(post("/accounts").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.balance").value(0))
                .andReturn().getResponse().getContentAsString();
        UUID accountId = UUID.fromString(JsonPath.read(created, "$.id"));

        updateBalance(token, accountId, "DEPOSIT", "100.00")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));

        updateBalance(token, accountId, "WITHDRAW", "30.50")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(69.50));

        updateBalance(token, accountId, "WITHDRAW", "69.51")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ExceptionMessages.INSUFFICIENT_BALANCE));

        mockMvc.perform(get("/accounts/{id}/balance", accountId).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(69.50));

        mockMvc.perform(put("/accounts/{id}/deactivate", accountId).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());

        updateBalance(token, accountId, "DEPOSIT", "10.00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ExceptionMessages.ACCOUNT_INACTIVE));

        Account persisted = accountRepository.findById(accountId).orElseThrow();
        assertThat(persisted.getBalance()).isEqualByComparingTo("69.50");
        assertThat(persisted.getStatus()).isEqualTo(AccountStatus.TERMINATED);
        assertThat(persisted.getAccountNumber()).matches("\\d{10}");
        assertThat(persisted.getAgency()).matches("\\d{4}");
    }

    @Test
    void createAccountForUserMissingInUserServiceReturnsNotFoundAndPersistsNothing() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userClient.getUserById(userId)).thenThrow(new UserNotFoundException(ExceptionMessages.USER_NOT_FOUND));

        mockMvc.perform(post("/accounts").header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearer(userId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ExceptionMessages.USER_NOT_FOUND));

        assertThat(accountRepository.findByUserId(userId)).isEmpty();
    }

    private ResultActions updateBalance(
            String token, UUID accountId, String type, String amount) throws Exception {
        return mockMvc.perform(put("/accounts/{id}/balance", accountId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\": \"%s\", \"amount\": %s}".formatted(type, amount)));
    }
}
