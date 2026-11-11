package com.gabroliv.financemanagement.application.ports.out;

import com.gabroliv.financemanagement.domain.model.Transaction;

import java.util.List;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);

    List<Transaction> findAllByUserId(Long userId);
}
