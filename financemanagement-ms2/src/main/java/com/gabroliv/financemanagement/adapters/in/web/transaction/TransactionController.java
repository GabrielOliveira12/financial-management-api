package com.gabroliv.financemanagement.adapters.in.web.transaction;

import com.gabroliv.financemanagement.adapters.in.web.transaction.dto.TransactionRequest;
import com.gabroliv.financemanagement.adapters.in.web.transaction.dto.TransactionResponse;
import com.gabroliv.financemanagement.application.command.CreateTransactionCommand;
import com.gabroliv.financemanagement.application.ports.in.TransactionUseCase;
import com.gabroliv.financemanagement.domain.model.Transaction;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionUseCase transactionUseCase;

    public TransactionController(TransactionUseCase transactionUseCase) {
        this.transactionUseCase = transactionUseCase;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@AuthenticationPrincipal Long authenticatedUserId,
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

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> findAll(@AuthenticationPrincipal Long authenticatedUserId) {
        List<TransactionResponse> transactions = transactionUseCase.findAllByUserId(authenticatedUserId).stream().map(this::toResponse).toList();

        return ResponseEntity.ok(transactions);
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(transaction.getId(), transaction.getUserId(), transaction.getDestinationUserId(),
                transaction.getType(), transaction.getAmount(), transaction.getOccurredAt(), transaction.getDescription(),
                transaction.getCategory(), transaction.getStatus(), transaction.getRejectionReason(), transaction.getCreatedAt());
    }
}
