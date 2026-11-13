package com.gabroliv.financemanagement.application.query;

import com.gabroliv.financemanagement.domain.model.TransactionType;

import java.time.LocalDate;

public record TransactionFilters(
        LocalDate startDate,
        LocalDate endDate,
        TransactionType type
) {}
