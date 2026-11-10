package com.gabroliv.financemanagement.application.service;

import com.gabroliv.financemanagement.adapters.out.mockapi.exception.AccountGatewayException;
import com.gabroliv.financemanagement.adapters.out.mockapi.exception.AccountNotFoundException;
import com.gabroliv.financemanagement.application.command.CreateTransactionCommand;
import com.gabroliv.financemanagement.application.ports.in.TransactionUseCase;
import com.gabroliv.financemanagement.application.ports.out.AccountGatewayPort;
import com.gabroliv.financemanagement.application.ports.out.AccountLimits;
import com.gabroliv.financemanagement.application.ports.out.TransactionRepositoryPort;
import com.gabroliv.financemanagement.domain.model.RejectionReason;
import com.gabroliv.financemanagement.domain.model.Transaction;
import com.gabroliv.financemanagement.domain.model.TransactionStatus;
import com.gabroliv.financemanagement.domain.model.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService implements TransactionUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountGatewayPort accountGatewayPort;

    public TransactionService(
            TransactionRepositoryPort transactionRepository,
            AccountGatewayPort accountGatewayPort
    ) {
        this.transactionRepository = transactionRepository;
        this.accountGatewayPort = accountGatewayPort;
    }

    @Override
    public Transaction create(Long userId, CreateTransactionCommand command) {
        Transaction transaction = createApprovedTransaction(userId, command);

        if (command.type() == TransactionType.DEPOSIT) {
            return transactionRepository.save(transaction);
        }

        try {
            AccountLimits accountLimits = accountGatewayPort.getAccountLimits(userId);

            RejectionReason rejectionReason = validateFinancialRules(
                    userId,
                    command,
                    accountLimits
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
    public List<Transaction> findAllByUserId(Long userId) {
        return transactionRepository.findAllByUserId(userId);
    }

    private Transaction createApprovedTransaction(
            Long userId,
            CreateTransactionCommand command
    ) {
        return new Transaction(
                null,
                userId,
                command.destinationUserId(),
                command.type(),
                command.amount(),
                command.occurredAt(),
                command.description(),
                command.category(),
                TransactionStatus.APPROVED,
                null,
                LocalDateTime.now()
        );
    }

    private RejectionReason validateFinancialRules(
            Long userId,
            CreateTransactionCommand command,
            AccountLimits accountLimits
    ) {
        if (command.amount().compareTo(accountLimits.transactionLimit()) > 0) {
            return RejectionReason.TRANSACTION_LIMIT_EXCEEDED;
        }

        if (command.amount().compareTo(accountLimits.balance()) > 0) {
            return RejectionReason.INSUFFICIENT_BALANCE;
        }

        BigDecimal dailyAmount = calculateApprovedDailyAmount(
                userId,
                command.occurredAt().toLocalDate()
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
