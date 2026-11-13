package com.gabroliv.financemanagement.domain.exception;

public class TransactionAccessDeniedException extends RuntimeException {

    public TransactionAccessDeniedException() {
        super("You do not have permission to access this transaction.");
    }
}
