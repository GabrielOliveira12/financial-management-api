package com.gabroliv.financemanagement.adapters.out.persistence.transaction;

import com.gabroliv.financemanagement.domain.model.RejectionReason;
import com.gabroliv.financemanagement.domain.model.TransactionCategory;
import com.gabroliv.financemanagement.domain.model.TransactionStatus;
import com.gabroliv.financemanagement.domain.model.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_transaction")
public class TransactionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "destination_user_id")
    private Long destinationUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TransactionCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "rejection_reason", length = 40)
    private RejectionReason rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected TransactionJpaEntity() {
    }

    public TransactionJpaEntity(Long id, Long userId, Long destinationUserId, TransactionType type,
                                BigDecimal amount, LocalDateTime occurredAt, String description,
                                TransactionCategory category, TransactionStatus status, RejectionReason rejectionReason,
                                LocalDateTime createdAt) {
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
