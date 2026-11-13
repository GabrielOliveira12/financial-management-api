package com.gabroliv.financemanagement.application.ports.in;

import com.gabroliv.financemanagement.adapters.in.web.expense.dto.ExpenseSummaryResponse;

import java.time.LocalDate;

public interface ExpenseUseCase {

    ExpenseSummaryResponse getSummary(Long userId, LocalDate startDate, LocalDate endDate);
}
