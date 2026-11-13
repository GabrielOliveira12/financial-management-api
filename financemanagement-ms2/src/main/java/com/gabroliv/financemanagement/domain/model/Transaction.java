package com.gabroliv.financemanagement.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final BigDecimal usdRate;
    private final LocalDate usdRateDate;
    private final BigDecimal amountInUsd;
    private final BigDecimal eurRate;
    private final LocalDate eurRateDate;
    private final BigDecimal amountInEur;

    public Transaction(Long id, Long userId, Long destinationUserId, TransactionType type, BigDecimal amount,
                       LocalDateTime occurredAt, String description, TransactionCategory category,
                       TransactionStatus status, RejectionReason rejectionReason, LocalDateTime createdAt,
                       BigDecimal usdRate, LocalDate usdRateDate, BigDecimal amountInUsd,
                       BigDecimal eurRate, LocalDate eurRateDate, BigDecimal amountInEur) {

        validateUserId(userId);
        validateAmount(amount);
        validateType(type);
        validateCategory(type, category);
        validateDestinationUser(userId, type, destinationUserId);
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
        this.usdRate = usdRate;
        this.usdRateDate = usdRateDate;
        this.amountInUsd = amountInUsd;
        this.eurRate = eurRate;
        this.eurRateDate = eurRateDate;
        this.amountInEur = amountInEur;

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
        if (type == TransactionType.PURCHASE && category == null) {
            throw new IllegalArgumentException("Category is required for purchase transactions");
        }

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

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Transaction user is required");
        }
    }

    private void validateDestinationUser(Long userId, TransactionType type, Long destinationUserId) {
        if (type == TransactionType.TRANSFER && destinationUserId == null) {
            throw new IllegalArgumentException("Destination user is required for transfer transactions");
        }

        if (type == TransactionType.TRANSFER && userId.equals(destinationUserId)) {
            throw new IllegalArgumentException("Destination user cannot be the same as the transaction user");
        }

        if (type != TransactionType.TRANSFER && destinationUserId != null) {
            throw new IllegalArgumentException("Destination user is only allowed for transfer transactions");
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

    public BigDecimal getUsdRate() {
        return usdRate;
    }

    public LocalDate getUsdRateDate() {
        return usdRateDate;
    }

    public BigDecimal getAmountInUsd() {
        return amountInUsd;
    }

    public BigDecimal getEurRate() {
        return eurRate;
    }

    public LocalDate getEurRateDate() {
        return eurRateDate;
    }

    public BigDecimal getAmountInEur() {
        return amountInEur;
    }
}
