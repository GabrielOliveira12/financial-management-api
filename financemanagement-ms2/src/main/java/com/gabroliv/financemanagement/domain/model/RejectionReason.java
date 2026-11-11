package com.gabroliv.financemanagement.domain.model;

public enum RejectionReason {
    INSUFFICIENT_BALANCE,
    TRANSACTION_LIMIT_EXCEEDED,
    DAILY_LIMIT_EXCEEDED,
    ACCOUNT_NOT_FOUND,
    ACCOUNT_SERVICE_UNAVAILABLE
}
