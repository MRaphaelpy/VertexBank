package com.mraphaelpy.accountservice.exception;

public class NoBalanceException extends RuntimeException {
    public NoBalanceException(String message) {
        super(message);
    }
}