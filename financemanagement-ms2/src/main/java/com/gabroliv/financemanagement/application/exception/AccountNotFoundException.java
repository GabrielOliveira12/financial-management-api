package com.gabroliv.financemanagement.application.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long userId) {
        super("Account not found for user: " + userId);
    }
}
