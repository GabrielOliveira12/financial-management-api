package com.gabroliv.financemanagement.adapters.in.web.transaction.dto;

import com.gabroliv.financemanagement.domain.model.TransactionCategory;
import com.gabroliv.financemanagement.domain.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRequest(
        @NotNull(message = "Transaction type is required.")
        TransactionType type,
        @NotNull(message = "Transaction amount is required.")
        @Positive(message = "Transaction amount must be greater than zero.")
        BigDecimal amount,
        Long destinationUserId,
        @NotNull(message = "Transaction occurrence date is required.")
        LocalDateTime occurredAt,
        String description,
        TransactionCategory category) { }
