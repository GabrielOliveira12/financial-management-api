package com.gabroliv.financemanagement.adapters.in.web.expense;

import com.gabroliv.financemanagement.adapters.in.web.expense.dto.ExpenseSummaryResponse;
import com.gabroliv.financemanagement.application.ports.in.ExpenseUseCase;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseUseCase expenseUseCase;

    public ExpenseController(ExpenseUseCase expenseUseCase) {
        this.expenseUseCase = expenseUseCase;
    }

    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryResponse> getSummary(
            @AuthenticationPrincipal Long authenticatedUserId,
            @Parameter(description = "Start date (inclusive). Format: yyyy-MM-dd. Example: 2026-11-01")
            @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "End date (inclusive). Format: yyyy-MM-dd. Example: 2026-11-30")
            @RequestParam(required = false) LocalDate endDate) {

        return ResponseEntity.ok(expenseUseCase.getSummary(authenticatedUserId, startDate, endDate));
    }
}
