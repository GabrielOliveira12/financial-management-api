package com.gabroliv.financemanagement.application.ports.out;

import com.gabroliv.financemanagement.domain.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(Long transactionId);

    List<Transaction> findAllByUserId(Long userId);
}
