package com.gabroliv.financemanagement.adapters.in.web.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseSummaryResponse(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalSpent,
        List<CategorySummary> byCategory,
        List<DaySummary> byDay,
        List<MonthSummary> byMonth
) {
    public record CategorySummary(String category, BigDecimal total) {}
    public record DaySummary(LocalDate date, BigDecimal total) {}
    public record MonthSummary(String month, BigDecimal total) {}
}
