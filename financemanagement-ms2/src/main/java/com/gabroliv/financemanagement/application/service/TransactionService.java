package com.gabroliv.financemanagement.application.service;

import com.gabroliv.financemanagement.application.command.CreateTransactionCommand;
import com.gabroliv.financemanagement.application.exception.AccountGatewayException;
import com.gabroliv.financemanagement.application.exception.AccountNotFoundException;
import com.gabroliv.financemanagement.application.ports.in.TransactionUseCase;
import com.gabroliv.financemanagement.application.ports.out.AccountGatewayPort;
import com.gabroliv.financemanagement.application.ports.out.AccountLimits;
import com.gabroliv.financemanagement.application.ports.out.TransactionRepositoryPort;
import com.gabroliv.financemanagement.application.query.TransactionFilters;
import com.gabroliv.financemanagement.domain.exception.TransactionAccessDeniedException;
import com.gabroliv.financemanagement.domain.exception.TransactionNotFoundException;
import com.gabroliv.financemanagement.domain.model.RejectionReason;
import com.gabroliv.financemanagement.domain.model.Transaction;
import com.gabroliv.financemanagement.domain.model.TransactionStatus;
import com.gabroliv.financemanagement.domain.model.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService implements TransactionUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountGatewayPort accountGatewayPort;
    private final Clock clock;

    public TransactionService(
            TransactionRepositoryPort transactionRepository,
            AccountGatewayPort accountGatewayPort,
            Clock clock
    ) {
        this.transactionRepository = transactionRepository;
        this.accountGatewayPort = accountGatewayPort;
        this.clock = clock;
    }

    @Override
    public Transaction create(Long userId, CreateTransactionCommand command) {
        LocalDateTime occurredAt = LocalDateTime.now(clock);
        LocalDate transactionDate = occurredAt.toLocalDate();

        Transaction transaction = createApprovedTransaction(
                userId,
                command,
                occurredAt
        );

        if (command.type() == TransactionType.DEPOSIT) {
            return transactionRepository.save(transaction);
        }

        try {
            AccountLimits accountLimits = accountGatewayPort.getAccountLimits(userId);

            RejectionReason rejectionReason = validateFinancialRules(
                    userId,
                    command,
                    accountLimits,
                    transactionDate
            );

            if (rejectionReason != null) {
                transaction.reject(rejectionReason);
            }
        } catch (AccountNotFoundException exception) {
            transaction.reject(RejectionReason.ACCOUNT_NOT_FOUND);
        } catch (AccountGatewayException exception) {
            transaction.reject(RejectionReason.ACCOUNT_SERVICE_UNAVAILABLE);
        }

        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction findById(Long authenticatedUserId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        if (!transaction.getUserId().equals(authenticatedUserId)) {
            throw new TransactionAccessDeniedException();
        }

        return transaction;
    }

    @Override
    public List<Transaction> findAllByUserId(Long authenticatedUserId, TransactionFilters filters) {
        return transactionRepository.findAllByUserId(authenticatedUserId).stream()
                .filter(t -> filters.type() == null || t.getType() == filters.type())
                .filter(t -> filters.startDate() == null || !t.getOccurredAt().toLocalDate().isBefore(filters.startDate()))
                .filter(t -> filters.endDate() == null || !t.getOccurredAt().toLocalDate().isAfter(filters.endDate()))
                .toList();
    }

    private Transaction createApprovedTransaction(
            Long userId,
            CreateTransactionCommand command,
            LocalDateTime occurredAt
    ) {
        return new Transaction(
                null,
                userId,
                command.destinationUserId(),
                command.type(),
                command.amount(),
                occurredAt,
                command.description(),
                command.category(),
                TransactionStatus.APPROVED,
                null,
                occurredAt
        );
    }

    private RejectionReason validateFinancialRules(
            Long userId,
            CreateTransactionCommand command,
            AccountLimits accountLimits,
            LocalDate transactionDate
    ) {
        if (command.amount().compareTo(accountLimits.transactionLimit()) > 0) {
            return RejectionReason.TRANSACTION_LIMIT_EXCEEDED;
        }

        if (command.amount().compareTo(accountLimits.balance()) > 0) {
            return RejectionReason.INSUFFICIENT_BALANCE;
        }

        BigDecimal dailyAmount = calculateApprovedDailyAmount(
                userId,
                transactionDate
        );

        BigDecimal dailyAmountAfterTransaction = dailyAmount.add(command.amount());

        if (dailyAmountAfterTransaction.compareTo(accountLimits.dailyLimit()) > 0) {
            return RejectionReason.DAILY_LIMIT_EXCEEDED;
        }

        return null;
    }

    private BigDecimal calculateApprovedDailyAmount(Long userId, LocalDate date) {
        return transactionRepository.findAllByUserId(userId).stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.APPROVED)
                .filter(transaction -> transaction.getType() != TransactionType.DEPOSIT)
                .filter(transaction -> transaction.getOccurredAt().toLocalDate().equals(date))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
