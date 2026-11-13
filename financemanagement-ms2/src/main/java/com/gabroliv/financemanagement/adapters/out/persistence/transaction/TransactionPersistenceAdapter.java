package com.gabroliv.financemanagement.adapters.out.persistence.transaction;

import com.gabroliv.financemanagement.application.ports.out.TransactionRepositoryPort;
import com.gabroliv.financemanagement.domain.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TransactionPersistenceAdapter implements TransactionRepositoryPort {

    private final SpringDataTransactionRepository transactionRepository;

    public TransactionPersistenceAdapter(SpringDataTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionJpaEntity entity = new TransactionJpaEntity(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getDestinationUserId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getOccurredAt(),
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getStatus(),
                transaction.getRejectionReason(),
                transaction.getCreatedAt(),
                transaction.getUsdRate(),
                transaction.getUsdRateDate(),
                transaction.getAmountInUsd(),
                transaction.getEurRate(),
                transaction.getEurRateDate(),
                transaction.getAmountInEur()
        );

        return transactionRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Transaction> findById(Long transactionId) {
        return transactionRepository.findById(transactionId).map(TransactionJpaEntity::toDomain);
    }

    @Override
    public List<Transaction> findAllByUserId(Long userId) {
        return transactionRepository.findAllByUserIdOrderByOccurredAtDesc(userId).stream()
                .map(TransactionJpaEntity::toDomain)
                .toList();
    }
}
