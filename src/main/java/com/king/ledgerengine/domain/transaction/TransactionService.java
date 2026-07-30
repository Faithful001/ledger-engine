package com.king.ledgerengine.domain.transaction;

import com.king.ledgerengine.domain.account.AccountRepository;
import com.king.ledgerengine.domain.account.entity.Account;
import com.king.ledgerengine.domain.entry.EntryRepository;
import com.king.ledgerengine.domain.entry.entity.Entry;
import com.king.ledgerengine.domain.entry.enums.EntryType;
import com.king.ledgerengine.domain.transaction.dto.CreateTransactionDto;
import com.king.ledgerengine.domain.transaction.dto.EntryLineDto;
import com.king.ledgerengine.domain.transaction.entity.Transaction;
import com.king.ledgerengine.domain.transaction.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final EntryRepository entryRepository;

    @Transactional
    public Transaction create(CreateTransactionDto payload, String idempotencyKey) {
        // Idempotency check — if this key was already processed, return the existing result
        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        validateBalanced(payload.getEntries());

        Transaction transaction = Transaction.builder()
                .description(payload.getDescription())
                .status(TransactionStatus.POSTED)
                .idempotencyKey(idempotencyKey)
                .build();

        transactionRepository.save(transaction);

        List<Entry> entries = new ArrayList<>();
        for (EntryLineDto line : payload.getEntries()) {
            Account account = accountRepository.findById(line.getAccountId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Account not found: " + line.getAccountId()));

            Entry entry = Entry.builder()
                    .transaction(transaction)
                    .account(account)
                    .amount(line.getAmount())
                    .type(line.getType())
                    .build();

            entries.add(entry);
        }

        entryRepository.saveAll(entries);
        return transaction;
    }

    private void validateBalanced(List<EntryLineDto> lines) {
        BigDecimal debits = BigDecimal.ZERO;
        BigDecimal credits = BigDecimal.ZERO;

        for (EntryLineDto line : lines) {
            if (line.getType() == EntryType.DEBIT) {
                debits = debits.add(line.getAmount());
            } else {
                credits = credits.add(line.getAmount());
            }
        }

        if (debits.compareTo(credits) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transaction is not balanced: debits=" + debits + " credits=" + credits);
        }
    }

    @Transactional
    public Transaction reverse(String transactionId) {
        Transaction original = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        if (original.getStatus() != TransactionStatus.POSTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only POSTED transactions can be reversed");
        }

        Transaction reversal = Transaction.builder()
                .description("Reversal of: " + original.getDescription())
                .status(TransactionStatus.POSTED)
                .idempotencyKey("reversal-" + original.getId())
                .build();

        transactionRepository.save(reversal);

        List<Entry> reversalEntries = new ArrayList<>();
        for (Entry original_entry : original.getEntries()) {
            EntryType flipped = original_entry.getType() == EntryType.DEBIT ? EntryType.CREDIT : EntryType.DEBIT;

            Entry reversalEntry = Entry.builder()
                    .transaction(reversal)
                    .account(original_entry.getAccount())
                    .amount(original_entry.getAmount())
                    .type(flipped)
                    .build();

            reversalEntries.add(reversalEntry);
        }

        entryRepository.saveAll(reversalEntries);

        original.setStatus(TransactionStatus.REVERSED);
        transactionRepository.save(original);

        return reversal;
    }
}