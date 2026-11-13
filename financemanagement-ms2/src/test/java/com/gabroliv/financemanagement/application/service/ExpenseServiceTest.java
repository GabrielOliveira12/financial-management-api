package com.gabroliv.financemanagement.application.service;

import com.gabroliv.financemanagement.adapters.in.web.expense.dto.ExpenseSummaryResponse;
import com.gabroliv.financemanagement.application.ports.out.TransactionRepositoryPort;
import com.gabroliv.financemanagement.domain.model.Transaction;
import com.gabroliv.financemanagement.domain.model.TransactionCategory;
import com.gabroliv.financemanagement.domain.model.TransactionStatus;
import com.gabroliv.financemanagement.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    private static final Long USER_ID = 1L;

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-11-15T12:00:00Z"),
            ZoneOffset.UTC
    );

    @Mock
    private TransactionRepositoryPort transactionRepository;

    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseService = new ExpenseService(transactionRepository, FIXED_CLOCK);
    }

    @Test
    void shouldSummarizeOnlyApprovedExpenseTransactions() {
        Transaction approvedPurchaseFood = transaction(
                1L, TransactionType.PURCHASE, new BigDecimal("100.00"),
                LocalDateTime.of(2026, 11, 5, 10, 0), TransactionCategory.FOOD,
                TransactionStatus.APPROVED, null
        );
        Transaction approvedPurchaseTransport = transaction(
                2L, TransactionType.PURCHASE, new BigDecimal("50.00"),
                LocalDateTime.of(2026, 11, 6, 10, 0), TransactionCategory.TRANSPORT,
                TransactionStatus.APPROVED, null
        );
        Transaction approvedWithdrawal = transaction(
                3L, TransactionType.WITHDRAWAL, new BigDecimal("200.00"),
                LocalDateTime.of(2026, 11, 7, 10, 0), null,
                TransactionStatus.APPROVED, null
        );
        Transaction approvedTransfer = transaction(
                4L, TransactionType.TRANSFER, new BigDecimal("30.00"),
                LocalDateTime.of(2026, 11, 7, 11, 0), null,
                TransactionStatus.APPROVED, null
        );
        Transaction rejectedPurchase = transaction(
                5L, TransactionType.PURCHASE, new BigDecimal("999.00"),
                LocalDateTime.of(2026, 11, 8, 10, 0), TransactionCategory.ENTERTAINMENT,
                TransactionStatus.REJECTED, com.gabroliv.financemanagement.domain.model.RejectionReason.INSUFFICIENT_BALANCE
        );
        Transaction approvedDeposit = transaction(
                6L, TransactionType.DEPOSIT, new BigDecimal("5000.00"),
                LocalDateTime.of(2026, 11, 9, 10, 0), null,
                TransactionStatus.APPROVED, null
        );

        when(transactionRepository.findAllByUserId(USER_ID)).thenReturn(List.of(
                approvedPurchaseFood,
                approvedPurchaseTransport,
                approvedWithdrawal,
                approvedTransfer,
                rejectedPurchase,
                approvedDeposit
        ));

        ExpenseSummaryResponse summary = expenseService.getSummary(
                USER_ID,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
        );

        assertEquals(new BigDecimal("380.00"), summary.totalSpent());

        BigDecimal categoryTotal = summary.byCategory().stream()
                .map(ExpenseSummaryResponse.CategorySummary::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("380.00"), categoryTotal);

        assertTrue(summary.byCategory().stream()
                .noneMatch(c -> c.category().equals(TransactionCategory.ENTERTAINMENT.name())));
    }

    @Test
    void shouldGroupExpensesByCategoryDayAndMonth() {
        Transaction purchase1 = transaction(
                1L, TransactionType.PURCHASE, new BigDecimal("30.00"),
                LocalDateTime.of(2026, 11, 5, 10, 0), TransactionCategory.FOOD,
                TransactionStatus.APPROVED, null
        );
        Transaction purchase2 = transaction(
                2L, TransactionType.PURCHASE, new BigDecimal("20.00"),
                LocalDateTime.of(2026, 11, 5, 15, 0), TransactionCategory.FOOD,
                TransactionStatus.APPROVED, null
        );
        Transaction purchase3 = transaction(
                3L, TransactionType.PURCHASE, new BigDecimal("40.00"),
                LocalDateTime.of(2026, 11, 6, 10, 0), TransactionCategory.HEALTH,
                TransactionStatus.APPROVED, null
        );

        when(transactionRepository.findAllByUserId(USER_ID)).thenReturn(List.of(purchase1, purchase2, purchase3));

        ExpenseSummaryResponse summary = expenseService.getSummary(
                USER_ID,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
        );

        assertEquals(new BigDecimal("90.00"), summary.totalSpent());

        assertEquals(2, summary.byCategory().size());
        assertTrue(summary.byCategory().stream()
                .anyMatch(c -> c.category().equals("FOOD") && c.total().compareTo(new BigDecimal("50.00")) == 0));
        assertTrue(summary.byCategory().stream()
                .anyMatch(c -> c.category().equals("HEALTH") && c.total().compareTo(new BigDecimal("40.00")) == 0));

        assertEquals(2, summary.byDay().size());
        assertTrue(summary.byDay().stream()
                .anyMatch(d -> d.date().equals(LocalDate.of(2026, 11, 5)) && d.total().compareTo(new BigDecimal("50.00")) == 0));
        assertTrue(summary.byDay().stream()
                .anyMatch(d -> d.date().equals(LocalDate.of(2026, 11, 6)) && d.total().compareTo(new BigDecimal("40.00")) == 0));

        assertEquals(1, summary.byMonth().size());
        assertEquals("2026-11", summary.byMonth().get(0).month());
        assertEquals(new BigDecimal("90.00"), summary.byMonth().get(0).total());
    }

    @Test
    void shouldUseCurrentMonthAsDefaultPeriodWhenNoDatesAreProvided() {
        when(transactionRepository.findAllByUserId(USER_ID)).thenReturn(List.of());

        ExpenseSummaryResponse summary = expenseService.getSummary(USER_ID, null, null);

        assertEquals(LocalDate.of(2026, 11, 1), summary.startDate());
        assertEquals(LocalDate.of(2026, 11, 30), summary.endDate());
        assertEquals(BigDecimal.ZERO, summary.totalSpent());
    }

    @Test
    void shouldExcludeExpensesOutsideInformedPeriod() {
        Transaction insidePeriod = transaction(
                1L, TransactionType.PURCHASE, new BigDecimal("100.00"),
                LocalDateTime.of(2026, 11, 10, 10, 0), TransactionCategory.FOOD,
                TransactionStatus.APPROVED, null
        );
        Transaction beforePeriod = transaction(
                2L, TransactionType.PURCHASE, new BigDecimal("500.00"),
                LocalDateTime.of(2026, 10, 31, 10, 0), TransactionCategory.FOOD,
                TransactionStatus.APPROVED, null
        );
        Transaction afterPeriod = transaction(
                3L, TransactionType.PURCHASE, new BigDecimal("700.00"),
                LocalDateTime.of(2026, 12, 1, 10, 0), TransactionCategory.FOOD,
                TransactionStatus.APPROVED, null
        );

        when(transactionRepository.findAllByUserId(USER_ID)).thenReturn(List.of(insidePeriod, beforePeriod, afterPeriod));

        ExpenseSummaryResponse summary = expenseService.getSummary(
                USER_ID,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
        );

        assertEquals(new BigDecimal("100.00"), summary.totalSpent());
    }

    private Transaction transaction(
            Long id,
            TransactionType type,
            BigDecimal amount,
            LocalDateTime occurredAt,
            TransactionCategory category,
            TransactionStatus status,
            com.gabroliv.financemanagement.domain.model.RejectionReason rejectionReason
    ) {
        Long destinationUserId = type == TransactionType.TRANSFER ? 2L : null;

        return new Transaction(
                id, USER_ID, destinationUserId, type, amount, occurredAt,
                "Test transaction", category, status, rejectionReason, occurredAt,
                null, null, null, null, null, null
        );
    }
}
