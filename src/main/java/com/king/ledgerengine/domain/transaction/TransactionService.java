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

    private static final int MIN_ENTRIES = 2;

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final EntryRepository entryRepository;

    @Transactional
    public Transaction create(CreateTransactionDto payload, String idempotencyKey, String userId) {
        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        validateMinimumEntries(payload.getEntries());
        validateBalanced(payload.getEntries());

        List<Account> accounts = resolveAccounts(payload.getEntries());
        validateUserIsInvolved(accounts, userId);

        Transaction transaction = Transaction.builder()
                .description(payload.getDescription())
                .status(TransactionStatus.POSTED)
                .idempotencyKey(idempotencyKey)
                .build();

        transactionRepository.save(transaction);

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < payload.getEntries().size(); i++) {
            EntryLineDto line = payload.getEntries().get(i);
            Account account = accounts.get(i);

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

    @Transactional
    public Transaction reverse(String transactionId, String userId) {
        Transaction original = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        if (!original.involvesUser(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to reverse this transaction");
        }

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
        for (Entry originalEntry : original.getEntries()) {
            EntryType flipped = originalEntry.getType() == EntryType.DEBIT ? EntryType.CREDIT : EntryType.DEBIT;

            Entry reversalEntry = Entry.builder()
                    .transaction(reversal)
                    .account(originalEntry.getAccount())
                    .amount(originalEntry.getAmount())
                    .type(flipped)
                    .build();

            reversalEntries.add(reversalEntry);
        }

        entryRepository.saveAll(reversalEntries);

        original.setStatus(TransactionStatus.REVERSED);
        transactionRepository.save(original);

        return reversal;
    }

    private List<Account> resolveAccounts(List<EntryLineDto> lines) {
        List<Account> accounts = new ArrayList<>();
        for (EntryLineDto line : lines) {
            Account account = accountRepository.findById(line.getAccountId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Account not found: " + line.getAccountId()));
            accounts.add(account);
        }
        return accounts;
    }

    private void validateUserIsInvolved(List<Account> accounts, String userId) {
        boolean involved = accounts.stream().anyMatch(account -> account.getUser().getId().equals(userId));
        if (!involved) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You must own at least one account in this transaction");
        }
    }

    private void validateMinimumEntries(List<EntryLineDto> lines) {
        if (lines == null || lines.size() < MIN_ENTRIES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A transaction requires at least " + MIN_ENTRIES + " entries");
        }
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
}