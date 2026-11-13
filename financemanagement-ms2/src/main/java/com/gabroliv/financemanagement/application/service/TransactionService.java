package com.gabroliv.financemanagement.application.service;

import com.gabroliv.financemanagement.application.command.CreateTransactionCommand;
import com.gabroliv.financemanagement.application.exception.AccountGatewayException;
import com.gabroliv.financemanagement.application.exception.AccountNotFoundException;
import com.gabroliv.financemanagement.application.ports.in.TransactionUseCase;
import com.gabroliv.financemanagement.application.ports.out.AccountGatewayPort;
import com.gabroliv.financemanagement.application.ports.out.AccountLimits;
import com.gabroliv.financemanagement.application.ports.out.CurrencyConversionPort;
import com.gabroliv.financemanagement.application.ports.out.CurrencyRate;
import com.gabroliv.financemanagement.application.ports.out.TransactionRepositoryPort;
import com.gabroliv.financemanagement.application.ports.out.UserGatewayPort;
import com.gabroliv.financemanagement.application.query.TransactionFilters;
import com.gabroliv.financemanagement.application.query.TransactionWithConversion;
import com.gabroliv.financemanagement.domain.exception.TransactionAccessDeniedException;
import com.gabroliv.financemanagement.domain.exception.TransactionNotFoundException;
import com.gabroliv.financemanagement.domain.model.RejectionReason;
import com.gabroliv.financemanagement.domain.model.Transaction;
import com.gabroliv.financemanagement.domain.model.TransactionStatus;
import com.gabroliv.financemanagement.domain.model.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService implements TransactionUseCase {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepositoryPort transactionRepository;
    private final AccountGatewayPort accountGatewayPort;
    private final UserGatewayPort userGatewayPort;
    private final CurrencyConversionPort currencyConversionPort;
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    public TransactionService(
            TransactionRepositoryPort transactionRepository,
            AccountGatewayPort accountGatewayPort,
            UserGatewayPort userGatewayPort,
            CurrencyConversionPort currencyConversionPort,
            JdbcTemplate jdbcTemplate,
            Clock clock
    ) {
        this.transactionRepository = transactionRepository;
        this.accountGatewayPort = accountGatewayPort;
        this.userGatewayPort = userGatewayPort;
        this.currencyConversionPort = currencyConversionPort;
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Transactional
    @Override
    public Transaction create(Long userId, CreateTransactionCommand command) {
        jdbcTemplate.execute(
                "SELECT pg_advisory_xact_lock(?)",
                (PreparedStatementCallback<Void>) ps -> {
                    ps.setLong(1, userId);
                    ps.execute();
                    return null;
                }
        );

        LocalDateTime occurredAt = LocalDateTime.now(clock);
        LocalDate transactionDate = occurredAt.toLocalDate();

        if (command.type() == TransactionType.TRANSFER
                && !userGatewayPort.existsById(command.destinationUserId())) {
            return transactionRepository.save(buildRejectedTransaction(
                    userId, command, occurredAt, RejectionReason.ACCOUNT_NOT_FOUND));
        }

        AccountLimits accountLimits;
        try {
            accountLimits = accountGatewayPort.getAccountLimits(userId);
        } catch (AccountNotFoundException exception) {
            return transactionRepository.save(buildRejectedTransaction(
                    userId, command, occurredAt, RejectionReason.ACCOUNT_NOT_FOUND));
        } catch (AccountGatewayException exception) {
            return transactionRepository.save(buildRejectedTransaction(
                    userId, command, occurredAt, RejectionReason.ACCOUNT_SERVICE_UNAVAILABLE));
        }

        Optional<CurrencyRate> usdRate = currencyConversionPort.getRate("USD", transactionDate);
        Optional<CurrencyRate> eurRate = currencyConversionPort.getRate("EUR", transactionDate);

        Transaction transaction = buildApprovedTransaction(userId, command, occurredAt, usdRate, eurRate);

        if (command.type() == TransactionType.DEPOSIT) {
            BigDecimal newBalance = accountLimits.balance().add(command.amount());
            updateBalanceSafely(userId, newBalance);
            return transactionRepository.save(transaction);
        }

        RejectionReason rejectionReason = validateFinancialRules(userId, command, accountLimits, transactionDate);

        if (rejectionReason != null) {
            transaction.reject(rejectionReason);
        } else {
            BigDecimal newBalance = accountLimits.balance().subtract(command.amount());
            updateBalanceSafely(userId, newBalance);

            if (command.type() == TransactionType.TRANSFER) {
                creditBalance(command.destinationUserId(), command.amount());
            }
        }

        return transactionRepository.save(transaction);
    }

    @Override
    public TransactionWithConversion findById(Long authenticatedUserId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        if (!transaction.getUserId().equals(authenticatedUserId)) {
            throw new TransactionAccessDeniedException();
        }

        return new TransactionWithConversion(transaction);
    }

    @Override
    public List<Transaction> findAllByUserId(Long authenticatedUserId, TransactionFilters filters) {
        return transactionRepository.findAllByUserId(authenticatedUserId).stream()
                .filter(t -> filters.type() == null || t.getType() == filters.type())
                .filter(t -> filters.startDate() == null || !t.getOccurredAt().toLocalDate().isBefore(filters.startDate()))
                .filter(t -> filters.endDate() == null || !t.getOccurredAt().toLocalDate().isAfter(filters.endDate()))
                .toList();
    }

    private Transaction buildApprovedTransaction(Long userId, CreateTransactionCommand command,
                                                  LocalDateTime occurredAt,
                                                  Optional<CurrencyRate> usdRate,
                                                  Optional<CurrencyRate> eurRate) {
        BigDecimal usdRateValue = usdRate.map(CurrencyRate::rate).orElse(null);
        LocalDate usdRateDate = usdRate.map(CurrencyRate::rateDate).orElse(null);
        BigDecimal amountInUsd = usdRate.map(r -> r.convert(command.amount())).orElse(null);

        BigDecimal eurRateValue = eurRate.map(CurrencyRate::rate).orElse(null);
        LocalDate eurRateDate = eurRate.map(CurrencyRate::rateDate).orElse(null);
        BigDecimal amountInEur = eurRate.map(r -> r.convert(command.amount())).orElse(null);

        return new Transaction(null, userId, command.destinationUserId(), command.type(),
                command.amount(), occurredAt, command.description(), command.category(),
                TransactionStatus.APPROVED, null, occurredAt,
                usdRateValue, usdRateDate, amountInUsd,
                eurRateValue, eurRateDate, amountInEur);
    }

    private Transaction buildRejectedTransaction(Long userId, CreateTransactionCommand command,
                                                  LocalDateTime occurredAt, RejectionReason reason) {
        return new Transaction(null, userId, command.destinationUserId(), command.type(),
                command.amount(), occurredAt, command.description(), command.category(),
                TransactionStatus.REJECTED, reason, occurredAt,
                null, null, null, null, null, null);
    }

    private RejectionReason validateFinancialRules(Long userId, CreateTransactionCommand command,
                                                    AccountLimits accountLimits, LocalDate transactionDate) {
        if (command.amount().compareTo(accountLimits.transactionLimit()) > 0) {
            return RejectionReason.TRANSACTION_LIMIT_EXCEEDED;
        }
        if (command.amount().compareTo(accountLimits.balance()) > 0) {
            return RejectionReason.INSUFFICIENT_BALANCE;
        }
        BigDecimal dailyAmount = calculateApprovedDailyAmount(userId, transactionDate);
        if (dailyAmount.add(command.amount()).compareTo(accountLimits.dailyLimit()) > 0) {
            return RejectionReason.DAILY_LIMIT_EXCEEDED;
        }
        return null;
    }

    private BigDecimal calculateApprovedDailyAmount(Long userId, LocalDate date) {
        return transactionRepository.findAllByUserId(userId).stream()
                .filter(t -> t.getStatus() == TransactionStatus.APPROVED)
                .filter(t -> t.getType() != TransactionType.DEPOSIT)
                .filter(t -> t.getOccurredAt().toLocalDate().equals(date))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void creditBalance(Long userId, BigDecimal amount) {
        try {
            AccountLimits accountLimits = accountGatewayPort.getAccountLimits(userId);
            BigDecimal newBalance = accountLimits.balance().add(amount);
            updateBalanceSafely(userId, newBalance);
        } catch (AccountNotFoundException | AccountGatewayException exception) {
            log.error("Could not credit balance for user {} on MockAPI.", userId, exception);
        }
    }

    private void updateBalanceSafely(Long userId, BigDecimal newBalance) {
        try {
            accountGatewayPort.updateBalance(userId, newBalance);
        } catch (AccountGatewayException exception) {
            log.error("Could not update balance for user {} on MockAPI.", userId, exception);
        }
    }
}
