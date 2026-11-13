package com.gabroliv.financemanagement.application.exception;

public class AccountGatewayException extends RuntimeException {
    public AccountGatewayException(Throwable cause) {
        super("Could not reach the account service.", cause);
    }
}
