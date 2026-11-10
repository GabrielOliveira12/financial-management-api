package com.gabroliv.financemanagement.adapters.out.mockapi.exception;

public class AccountGatewayException extends RuntimeException {
    public AccountGatewayException(Throwable cause) {
        super("Não foi possível consultar o serviço de contas.", cause);
    }
}
