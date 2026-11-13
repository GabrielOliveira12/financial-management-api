package com.gabroliv.financemanagement.adapters.in.web.transaction;

import com.gabroliv.financemanagement.adapters.in.web.transaction.dto.TransactionRequest;
import com.gabroliv.financemanagement.adapters.in.web.transaction.dto.TransactionResponse;
import com.gabroliv.financemanagement.application.command.CreateTransactionCommand;
import com.gabroliv.financemanagement.application.ports.in.TransactionUseCase;
import com.gabroliv.financemanagement.application.query.TransactionFilters;
import com.gabroliv.financemanagement.application.query.TransactionWithConversion;
import com.gabroliv.financemanagement.domain.model.Transaction;
import com.gabroliv.financemanagement.domain.model.TransactionType;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionUseCase transactionUseCase;

    public TransactionController(TransactionUseCase transactionUseCase) {
        this.transactionUseCase = transactionUseCase;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @AuthenticationPrincipal Long authenticatedUserId,
            @Valid @RequestBody TransactionRequest request) {

        Transaction transaction = transactionUseCase.create(
                authenticatedUserId,
                new CreateTransactionCommand(
                        request.type(),
                        request.amount(),
                        request.destinationUserId(),
                        request.category(),
                        request.description()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(transaction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(
            @AuthenticationPrincipal Long authenticatedUserId,
            @PathVariable Long id) {

        TransactionWithConversion result = transactionUseCase.findById(authenticatedUserId, id);
        return ResponseEntity.ok(toResponse(result.transaction()));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> findAll(
            @AuthenticationPrincipal Long authenticatedUserId,
            @RequestParam(required = false) TransactionType type,
            @Parameter(description = "Start date (inclusive). Format: yyyy-MM-dd. Example: 2026-11-01")
            @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "End date (inclusive). Format: yyyy-MM-dd. Example: 2026-11-30")
            @RequestParam(required = false) LocalDate endDate) {

        TransactionFilters filters = new TransactionFilters(startDate, endDate, type);
        List<TransactionResponse> transactions = transactionUseCase
                .findAllByUserId(authenticatedUserId, filters)
                .stream().map(this::toResponse).toList();

        return ResponseEntity.ok(transactions);
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getUserId(),
                t.getDestinationUserId(),
                t.getType(),
                t.getAmount(),
                t.getOccurredAt(),
                t.getDescription(),
                t.getCategory(),
                t.getStatus(),
                t.getRejectionReason(),
                t.getCreatedAt(),
                t.getAmountInUsd(),
                t.getUsdRate(),
                t.getUsdRateDate(),
                t.getAmountInEur(),
                t.getEurRate(),
                t.getEurRateDate()
        );
    }
}
