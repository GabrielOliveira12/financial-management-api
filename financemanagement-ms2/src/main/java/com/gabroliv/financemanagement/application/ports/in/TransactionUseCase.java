package com.gabroliv.financemanagement.application.ports.in;

import com.gabroliv.financemanagement.application.command.CreateTransactionCommand;
import com.gabroliv.financemanagement.application.query.TransactionFilters;
import com.gabroliv.financemanagement.application.query.TransactionWithConversion;
import com.gabroliv.financemanagement.domain.model.Transaction;

import java.util.List;

public interface TransactionUseCase {

    Transaction create(Long userId, CreateTransactionCommand command);

    TransactionWithConversion findById(Long authenticatedUserId, Long transactionId);

    List<Transaction> findAllByUserId(Long authenticatedUserId, TransactionFilters filters);
}
