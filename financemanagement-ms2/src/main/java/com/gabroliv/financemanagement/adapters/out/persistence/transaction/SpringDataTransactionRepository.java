package com.gabroliv.financemanagement.adapters.out.persistence.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, Long> {

    List<TransactionJpaEntity> findAllByUserIdOrderByOccurredAtDesc(Long userId);
}
