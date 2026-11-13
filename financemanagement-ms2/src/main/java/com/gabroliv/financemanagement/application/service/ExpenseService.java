package com.gabroliv.financemanagement.application.service;

import com.gabroliv.financemanagement.adapters.in.web.expense.dto.ExpenseSummaryResponse;
import com.gabroliv.financemanagement.application.ports.in.ExpenseUseCase;
import com.gabroliv.financemanagement.application.ports.out.TransactionRepositoryPort;
import com.gabroliv.financemanagement.domain.model.Transaction;
import com.gabroliv.financemanagement.domain.model.TransactionStatus;
import com.gabroliv.financemanagement.domain.model.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService implements ExpenseUseCase {

    private static final List<TransactionType> EXPENSE_TYPES = List.of(
            TransactionType.WITHDRAWAL,
            TransactionType.TRANSFER,
            TransactionType.PURCHASE
    );

    private final TransactionRepositoryPort transactionRepository;
    private final Clock clock;

    public ExpenseService(TransactionRepositoryPort transactionRepository, Clock clock) {
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    @Override
    public ExpenseSummaryResponse getSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            YearMonth currentMonth = YearMonth.now(clock);
            startDate = currentMonth.atDay(1);
            endDate = currentMonth.atEndOfMonth();
        }

        final LocalDate start = startDate;
        final LocalDate end = endDate;

        List<Transaction> expenses = transactionRepository.findAllByUserId(userId).stream()
                .filter(t -> t.getStatus() == TransactionStatus.APPROVED)
                .filter(t -> EXPENSE_TYPES.contains(t.getType()))
                .filter(t -> !t.getOccurredAt().toLocalDate().isBefore(start))
                .filter(t -> !t.getOccurredAt().toLocalDate().isAfter(end))
                .toList();

        BigDecimal totalSpent = expenses.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ExpenseSummaryResponse.CategorySummary> byCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory().name() : "UNCATEGORIZED",
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(e -> new ExpenseSummaryResponse.CategorySummary(e.getKey(), e.getValue()))
                .toList();

        List<ExpenseSummaryResponse.DaySummary> byDay = expenses.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getOccurredAt().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new ExpenseSummaryResponse.DaySummary(e.getKey(), e.getValue()))
                .toList();

        List<ExpenseSummaryResponse.MonthSummary> byMonth = expenses.stream()
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getOccurredAt()),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new ExpenseSummaryResponse.MonthSummary(
                        e.getKey().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        e.getValue()
                ))
                .toList();

        return new ExpenseSummaryResponse(start, end, totalSpent, byCategory, byDay, byMonth);
    }
}
