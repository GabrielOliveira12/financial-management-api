package com.gabroliv.financemanagement.adapters.in.web.transaction.dto;

import com.gabroliv.financemanagement.domain.model.RejectionReason;
import com.gabroliv.financemanagement.domain.model.TransactionCategory;
import com.gabroliv.financemanagement.domain.model.TransactionStatus;
import com.gabroliv.financemanagement.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long userId,
        Long destinationUserId,
        TransactionType type,
        BigDecimal amount,
        LocalDateTime occurredAt,
        String description,
        TransactionCategory category,
        TransactionStatus status,
        RejectionReason rejectionReason,
        LocalDateTime createdAt
) {
}
