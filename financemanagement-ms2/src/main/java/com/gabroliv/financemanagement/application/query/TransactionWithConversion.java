package com.gabroliv.financemanagement.application.query;

import com.gabroliv.financemanagement.domain.model.Transaction;

public record TransactionWithConversion(
        Transaction transaction
) {}
