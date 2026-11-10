package com.gabroliv.financemanagement.application.service;

import com.gabroliv.financemanagement.application.command.CreateTransactionCommand;
import com.gabroliv.financemanagement.application.exception.AccountGatewayException;
import com.gabroliv.financemanagement.application.ports.out.AccountGatewayPort;
import com.gabroliv.financemanagement.application.ports.out.AccountLimits;
import com.gabroliv.financemanagement.application.ports.out.TransactionRepositoryPort;
import com.gabroliv.financemanagement.domain.model.RejectionReason;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final Long USER_ID = 1L;

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-11-10T15:00:00Z"),
            ZoneOffset.UTC
    );

    @Mock
    private TransactionRepositoryPort transactionRepository;

    @Mock
    private AccountGatewayPort accountGatewayPort;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository,
                accountGatewayPort,
                FIXED_CLOCK
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldApproveDepositWithoutConsultingAccountGateway() {
        CreateTransactionCommand command = new CreateTransactionCommand(
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                null,
                null,
                "Salary deposit"
        );

        Transaction transaction = transactionService.create(USER_ID, command);

        assertEquals(TransactionStatus.APPROVED, transaction.getStatus());
        assertEquals(null, transaction.getRejectionReason());
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
        assertEquals(
                LocalDateTime.of(2026, 11, 10, 15, 0),
                transaction.getOccurredAt()
        );
        assertEquals(
                LocalDateTime.of(2026, 11, 10, 15, 0),
                transaction.getCreatedAt()
        );

        verify(transactionRepository).save(transaction);
        verifyNoInteractions(accountGatewayPort);
        verify(transactionRepository, never()).findAllByUserId(USER_ID);
    }

    @Test
    void shouldRejectPurchaseWhenBalanceIsInsufficient() {
        when(accountGatewayPort.getAccountLimits(USER_ID))
                .thenReturn(new AccountLimits(
                        USER_ID,
                        new BigDecimal("100.00"),
                        new BigDecimal("1000.00"),
                        new BigDecimal("500.00")
                ));

        CreateTransactionCommand command = new CreateTransactionCommand(
                TransactionType.PURCHASE,
                new BigDecimal("150.00"),
                null,
                TransactionCategory.FOOD,
                "Supermarket purchase"
        );

        Transaction transaction = transactionService.create(USER_ID, command);

        assertEquals(TransactionStatus.REJECTED, transaction.getStatus());
        assertEquals(
                RejectionReason.INSUFFICIENT_BALANCE,
                transaction.getRejectionReason()
        );

        verify(accountGatewayPort).getAccountLimits(USER_ID);
        verify(transactionRepository).save(transaction);
        verify(transactionRepository, never()).findAllByUserId(USER_ID);
    }


    @Test
    void shouldRejectPurchaseWhenTransactionLimitIsExceeded() {
        when(accountGatewayPort.getAccountLimits(USER_ID))
                .thenReturn(new AccountLimits(
                        USER_ID,
                        new BigDecimal("5000.00"),
                        new BigDecimal("3000.00"),
                        new BigDecimal("100.00")
                ));

        CreateTransactionCommand command = new CreateTransactionCommand(
                TransactionType.PURCHASE,
                new BigDecimal("150.00"),
                null,
                TransactionCategory.FOOD,
                "Purchase above transaction limit"
        );

        Transaction transaction = transactionService.create(USER_ID, command);

        assertEquals(TransactionStatus.REJECTED, transaction.getStatus());
        assertEquals(
                RejectionReason.TRANSACTION_LIMIT_EXCEEDED,
                transaction.getRejectionReason()
        );

        verify(accountGatewayPort).getAccountLimits(USER_ID);
        verify(transactionRepository).save(transaction);
        verify(transactionRepository, never()).findAllByUserId(USER_ID);
    }

    @Test
    void shouldRejectPurchaseWhenDailyLimitIsExceeded() {
        Transaction previousApprovedPurchase = new Transaction(
                10L,
                USER_ID,
                null,
                TransactionType.PURCHASE,
                new BigDecimal("900.00"),
                LocalDateTime.of(2026, 11, 10, 9, 0),
                "Previous purchase",
                TransactionCategory.FOOD,
                TransactionStatus.APPROVED,
                null,
                LocalDateTime.of(2026, 11, 10, 9, 0)
        );

        when(accountGatewayPort.getAccountLimits(USER_ID))
                .thenReturn(new AccountLimits(
                        USER_ID,
                        new BigDecimal("5000.00"),
                        new BigDecimal("1000.00"),
                        new BigDecimal("500.00")
                ));

        when(transactionRepository.findAllByUserId(USER_ID))
                .thenReturn(List.of(previousApprovedPurchase));

        CreateTransactionCommand command = new CreateTransactionCommand(
                TransactionType.PURCHASE,
                new BigDecimal("150.00"),
                null,
                TransactionCategory.FOOD,
                "Purchase above daily limit"
        );

        Transaction transaction = transactionService.create(USER_ID, command);

        assertEquals(TransactionStatus.REJECTED, transaction.getStatus());
        assertEquals(
                RejectionReason.DAILY_LIMIT_EXCEEDED,
                transaction.getRejectionReason()
        );

        verify(accountGatewayPort).getAccountLimits(USER_ID);
        verify(transactionRepository).findAllByUserId(USER_ID);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void shouldRejectTransactionWhenAccountGatewayIsUnavailable() {
        when(accountGatewayPort.getAccountLimits(eq(USER_ID)))
                .thenThrow(new AccountGatewayException(
                        new RuntimeException("MockAPI unavailable")
                ));

        CreateTransactionCommand command = new CreateTransactionCommand(
                TransactionType.PURCHASE,
                new BigDecimal("150.00"),
                null,
                TransactionCategory.FOOD,
                "Purchase while MockAPI is unavailable"
        );

        Transaction transaction = transactionService.create(USER_ID, command);

        assertEquals(TransactionStatus.REJECTED, transaction.getStatus());
        assertEquals(
                RejectionReason.ACCOUNT_SERVICE_UNAVAILABLE,
                transaction.getRejectionReason()
        );

        verify(accountGatewayPort).getAccountLimits(USER_ID);
        verify(transactionRepository).save(transaction);
        verify(transactionRepository, never()).findAllByUserId(USER_ID);
    }
}
