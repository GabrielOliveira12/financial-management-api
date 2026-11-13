package com.gabroliv.financemanagement.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTest {

    @Test
    void shouldThrowExceptionWhenAmountIsZero() {
        assertThrows(IllegalArgumentException.class, () -> new Transaction(null, 1L, null,
                TransactionType.DEPOSIT, BigDecimal.ZERO, LocalDateTime.now(), "Deposit", null,
                TransactionStatus.APPROVED, null, LocalDateTime.now(),
                null, null, null, null, null, null));
    }

    @Test
    void shouldThrowExceptionWhenPurchaseHasNoCategory() {
        assertThrows(IllegalArgumentException.class, () -> new Transaction(null, 1L, null,
                TransactionType.PURCHASE, new BigDecimal("50.00"), LocalDateTime.now(),
                "Supermarket purchase", null, TransactionStatus.APPROVED, null,
                LocalDateTime.now(),
                null, null, null, null, null, null));
    }

    @Test
    void shouldThrowExceptionWhenTransferHasNoDestinationUser() {
        assertThrows(IllegalArgumentException.class, () -> new Transaction(null, 1L, null,
                TransactionType.TRANSFER, new BigDecimal("100.00"), LocalDateTime.now(), "Transfer",
                null, TransactionStatus.APPROVED, null, LocalDateTime.now(),
                null, null, null, null, null, null));
    }

    @Test
    void shouldThrowExceptionWhenRejectedTransactionHasNoReason() {
        assertThrows(IllegalArgumentException.class, () -> new Transaction(null, 1L, null,
                TransactionType.DEPOSIT, new BigDecimal("100.00"), LocalDateTime.now(), "Deposit",
                null, TransactionStatus.REJECTED, null, LocalDateTime.now(),
                null, null, null, null, null, null));
    }
}
