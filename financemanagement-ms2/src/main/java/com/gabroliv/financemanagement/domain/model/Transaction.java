package com.gabroliv.financemanagement.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private final Long id;
    private final Long userId;
    private final Long destinationUserId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final LocalDateTime occurredAt;
    private final String description;
    private final TransactionCategory category;
    private final TransactionStatus status;
    private final RejectionReason rejectionReason;
    private final LocalDateTime createdAt;

    public Transaction(Long id, Long userId, Long destinationUserId, TransactionType type, BigDecimal amount,
                       LocalDateTime occurredAt, String description, TransactionCategory category,
                       TransactionStatus status, RejectionReason rejectionReason, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.destinationUserId = destinationUserId;
        this.type = type;
        this.amount = amount;
        this.occurredAt = occurredAt;
        this.description = description;
        this.category = category;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getDestinationUserId() {
        return destinationUserId;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getDescription() {
        return description;
    }

    public TransactionCategory getCategory() {
        return category;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public RejectionReason getRejectionReason() {
        return rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
