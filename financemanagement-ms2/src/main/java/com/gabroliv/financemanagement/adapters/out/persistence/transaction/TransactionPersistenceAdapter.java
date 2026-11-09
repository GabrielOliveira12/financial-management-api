package com.gabroliv.financemanagement.adapters.out.persistence.transaction;

import com.gabroliv.financemanagement.application.port.out.TransactionRepositoryPort;
import com.gabroliv.financemanagement.domain.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionPersistenceAdapter implements TransactionRepositoryPort {

    private final SpringDataTransactionRepository transactionRepository;

    public TransactionPersistenceAdapter(SpringDataTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionJpaEntity savedEntity = transactionRepository.save(toEntity(transaction));

        return toDomain(savedEntity);
    }

    @Override
    public List<Transaction> findAllByUserId(Long userId) {
        return transactionRepository.findAllByUserIdOrderByOccurredAtDesc(userId).stream().map(this::toDomain).toList();
    }

    private TransactionJpaEntity toEntity(Transaction transaction) {
        return new TransactionJpaEntity(transaction.getId(), transaction.getUserId(), transaction.getDestinationUserId(),
                transaction.getType(), transaction.getAmount(), transaction.getOccurredAt(), transaction.getDescription(),
                transaction.getCategory(), transaction.getStatus(), transaction.getRejectionReason(),
                transaction.getCreatedAt());
    }

    private Transaction toDomain(TransactionJpaEntity entity) {
        return new Transaction(entity.getId(), entity.getUserId(), entity.getDestinationUserId(), entity.getType(),
                entity.getAmount(), entity.getOccurredAt(), entity.getDescription(), entity.getCategory(),
                entity.getStatus(), entity.getRejectionReason(), entity.getCreatedAt());
    }
}
