package com.king.ledgerengine.domain.transaction;

import com.king.ledgerengine.domain.transaction.dto.CreateTransactionDto;
import com.king.ledgerengine.domain.transaction.entity.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Create and reverse ledger transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @Operation(summary = "Create a new transaction", description = "Creates a balanced transaction with debit/credit entries. Requires an Idempotency-Key header.")
    @ApiResponse(responseCode = "201", description = "Transaction created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request (e.g., unbalanced entries)")
    @ApiResponse(responseCode = "403", description = "You do not own any account in this transaction")
    @PostMapping
    public ResponseEntity<Transaction> create(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateTransactionDto payload,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        Transaction transaction = transactionService.create(payload, idempotencyKey, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @Operation(summary = "Reverse a transaction", description = "Reverses a transaction")
    @ApiResponse(responseCode = "403", description = "You are not authorized to reverse this transaction")
    @PostMapping("/{id}/reverse")
    public Transaction reverse(@AuthenticationPrincipal String userId, @PathVariable String id) {
        return transactionService.reverse(id, userId);
    }
}