package com.mraphaelpy.accountservice.controller;

import com.mraphaelpy.accountservice.dtos.AccountResponse;
import com.mraphaelpy.accountservice.dtos.UpdateBalanceRequest;
import com.mraphaelpy.accountservice.services.AccountCommandService;
import com.mraphaelpy.accountservice.services.AccountQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountCommandService accountCommandService;
    private final AccountQueryService accountQueryService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @RequestAttribute("userId") UUID userId) {
        AccountResponse response = accountCommandService.createAccount(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getMyAccount(
            @RequestAttribute("userId") UUID userId) {
        AccountResponse response = accountQueryService.getAccountByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable("accountId") UUID accountId) {
        AccountResponse response = accountQueryService.getAccountById(accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable("accountId") UUID accountId) {
        BigDecimal balance = accountQueryService.getBalance(accountId);
        return ResponseEntity.ok(balance);
    }

    @PutMapping("/{accountId}/balance")
    public ResponseEntity<AccountResponse> updateBalance(
            @PathVariable("accountId") UUID accountId,
            @RequestBody @Valid UpdateBalanceRequest request) {
        AccountResponse response = accountCommandService.updateBalance(accountId, request.getAmount(), request.getType());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{accountId}/deactivate")
    public ResponseEntity<Void> deactivateAccount(
            @PathVariable("accountId") UUID accountId) {
        accountCommandService.deactivateAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{accountId}/reactivate")
    public ResponseEntity<Void> reactivateAccount(
            @PathVariable("accountId") UUID accountId) {
        accountCommandService.reactivateAccount(accountId);
        return ResponseEntity.noContent().build();
    }
}
