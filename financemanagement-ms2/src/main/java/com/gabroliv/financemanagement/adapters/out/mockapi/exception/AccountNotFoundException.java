package com.gabroliv.financemanagement.adapters.out.mockapi.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long userId) {
        super("Conta não encontrada para o usuário: " + userId);
    }
}
