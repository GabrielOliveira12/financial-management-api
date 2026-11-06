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
    private TransactionStatus status;
    private RejectionReason rejectionReason;
    private final LocalDateTime createdAt;

    public Transaction(Long id, Long userId, Long destinationUserId, TransactionType type, BigDecimal amount, LocalDateTime occurredAt,
                       String description, TransactionCategory category, TransactionStatus status, RejectionReason rejectionReason,
                       LocalDateTime createdAt) {

        validateAmount(amount);
        validateType(type);
        validateCategory(type, category);
        validateStatus(status, rejectionReason);

        this.id = id;
        this.userId = userId;
        this.destinationUserId = destinationUserId;
        this.type = type;
        this.amount = amount;
        this.occurredAt = occurredAt;
        this.description = description;
        this.category = category;
        this.createdAt = createdAt;

        if (status == TransactionStatus.APPROVED) {
            approve();
        } else {
            reject(rejectionReason);
        }
    }

    public void approve() {
        this.status = TransactionStatus.APPROVED;
        this.rejectionReason = null;
    }

    public void reject(RejectionReason rejectionReason) {
        if (rejectionReason == null) {
            throw new IllegalArgumentException("Rejection reason is required for rejected transactions");
        }

        this.status = TransactionStatus.REJECTED;
        this.rejectionReason = rejectionReason;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }
    }

    private void validateType(TransactionType type) {
        if (type == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }
    }

    private void validateCategory(TransactionType type, TransactionCategory category) {
        if (type != TransactionType.PURCHASE && category != null) {
            throw new IllegalArgumentException("Category is only allowed for purchase transactions");
        }
    }

    private void validateStatus(TransactionStatus status, RejectionReason rejectionReason) {
        if (status == null) {
            throw new IllegalArgumentException("Transaction status is required");
        }

        if (status == TransactionStatus.APPROVED && rejectionReason != null) {
            throw new IllegalArgumentException("Approved transactions cannot have a rejection reason");
        }
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
