package com.gabroliv.financemanagement.application.command;

import com.gabroliv.financemanagement.domain.model.TransactionCategory;
import com.gabroliv.financemanagement.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateTransactionCommand(TransactionType type, BigDecimal amount, Long destinationUserId,
                                       TransactionCategory category, String description) { }
