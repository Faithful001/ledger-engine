package com.king.ledgerengine.domain.transaction;

import com.king.ledgerengine.domain.transaction.dto.CreateTransactionDto;
import com.king.ledgerengine.domain.transaction.entity.Transaction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> create(
            @Valid @RequestBody CreateTransactionDto payload,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        Transaction transaction = transactionService.create(payload, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/{id}/reverse")
    public Transaction reverse(@PathVariable String id) {
        return transactionService.reverse(id);
    }
}