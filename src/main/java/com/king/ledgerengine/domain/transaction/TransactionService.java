package com.king.ledgerengine.domain.transaction;

import com.king.ledgerengine.domain.account.AccountRepository;
import com.king.ledgerengine.domain.account.entity.Account;
import com.king.ledgerengine.domain.account.enums.AccountOwnerType;
import com.king.ledgerengine.domain.account.enums.AccountType;
import com.king.ledgerengine.domain.entry.EntryRepository;
import com.king.ledgerengine.domain.entry.entity.Entry;
import com.king.ledgerengine.domain.entry.enums.EntryType;
import com.king.ledgerengine.domain.transaction.dto.CreateTransactionDto;
import com.king.ledgerengine.domain.transaction.dto.DepositDto;
import com.king.ledgerengine.domain.transaction.dto.EntryLineDto;
import com.king.ledgerengine.domain.transaction.entity.Transaction;
import com.king.ledgerengine.domain.transaction.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final int MIN_ENTRIES = 2;

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final EntryRepository entryRepository;

    @Transactional
    public Transaction create(CreateTransactionDto payload, String idempotencyKey, String userId) {
        return create(payload, idempotencyKey, userId, false);
    }

    @Transactional
    public Transaction createInternal(CreateTransactionDto payload, String idempotencyKey, String userId) {
        return create(payload, idempotencyKey, userId, true);
    }

    private Transaction create(CreateTransactionDto payload, String idempotencyKey, String userId, boolean allowSystemAccounts) {
        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        validateMinimumEntries(payload.getEntries());
        validateBalanced(payload.getEntries());

        List<Account> accounts = resolveAccounts(payload.getEntries());

        if (!allowSystemAccounts) {
            boolean touchesSystemAccount = accounts.stream()
                    .anyMatch(account -> account.getOwnerType() == AccountOwnerType.SYSTEM);
            if (touchesSystemAccount) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This operation cannot reference system accounts");
            }
        }

        validateUserIsInvolved(accounts, userId);
        validateSufficientBalances(payload.getEntries());

        Transaction transaction = Transaction.builder()
                .description(payload.getDescription())
                .status(TransactionStatus.POSTED)
                .idempotencyKey(idempotencyKey)
                .build();

        try {
            transactionRepository.save(transaction);
        } catch (DataIntegrityViolationException e) {
            return transactionRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> e);
        }

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

    public Transaction deposit(DepositDto dto, String idempotencyKey, String userId) {
        Account systemAccount = accountRepository.findFirstByOwnerType(AccountOwnerType.SYSTEM)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "System account not configured"));

        EntryLineDto debitSystem = new EntryLineDto();
        debitSystem.setAccountId(systemAccount.getId());
        debitSystem.setAmount(dto.getAmount());
        debitSystem.setType(EntryType.DEBIT);

        EntryLineDto creditUser = new EntryLineDto();
        creditUser.setAccountId(dto.getAccountId());
        creditUser.setAmount(dto.getAmount());
        creditUser.setType(EntryType.CREDIT);

        CreateTransactionDto txDto = new CreateTransactionDto();
        txDto.setDescription(dto.getDescription());
        txDto.setEntries(List.of(debitSystem, creditUser));

        // Note: userId here is the acting/requesting user, not necessarily the
        // owner of the account being credited — see ownership discussion below.
        return create(txDto, idempotencyKey, userId);
    }

    @Transactional
    public Transaction reverse(String transactionId, String userId) {
        Transaction original = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        if (!original.involvesUser(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to reverse this transaction");
        }

        if (original.getStatus() != TransactionStatus.POSTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only POSTED transactions can be reversed");
        }

        List<EntryLineDto> flippedLines = original.getEntries().stream()
                .map(entry -> {
                    EntryLineDto line = new EntryLineDto();
                    line.setAccountId(entry.getAccount().getId());
                    line.setAmount(entry.getAmount());
                    line.setType(entry.getType() == EntryType.DEBIT ? EntryType.CREDIT : EntryType.DEBIT);
                    return line;
                })
                .toList();

        validateSufficientBalances(flippedLines);

        Transaction reversal = Transaction.builder()
                .description("Reversal of: " + original.getDescription())
                .status(TransactionStatus.POSTED)
                .idempotencyKey("reversal-" + original.getId())
                .build();

        try {
            transactionRepository.save(reversal);
        } catch (DataIntegrityViolationException e) {
            return transactionRepository.findByIdempotencyKey(reversal.getIdempotencyKey())
                    .orElseThrow(() -> e);
        }

        List<Entry> reversalEntries = original.getEntries().stream()
                .map(originalEntry -> Entry.builder()
                        .transaction(reversal)
                        .account(originalEntry.getAccount())
                        .amount(originalEntry.getAmount())
                        .type(originalEntry.getType() == EntryType.DEBIT ? EntryType.CREDIT : EntryType.DEBIT)
                        .build())
                .toList();

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

    // Makes sure debited accounts have enough balance.
    // Locks them in sorted order to avoid deadlocks between concurrent transactions.
    private void validateSufficientBalances(List<EntryLineDto> lines) {
        Map<String, BigDecimal> totalDebitsByAccount = new HashMap<>();
        for (EntryLineDto line : lines) {
            if (line.getType() == EntryType.DEBIT) {
                totalDebitsByAccount.merge(line.getAccountId(), line.getAmount(), BigDecimal::add);
            }
        }

        List<String> debitAccountIds = totalDebitsByAccount.keySet().stream()
                .sorted()
                .toList();

        for (String accountId : debitAccountIds) {
            Account account = accountRepository.findByIdWithLock(accountId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Account not found: " + accountId));

            if (account.getOwnerType() != AccountOwnerType.SYSTEM) {
                continue;
            }

            BigDecimal currentBalance = entryRepository.getBalance(account.getId());
            BigDecimal totalDebit = totalDebitsByAccount.get(accountId);

            if (currentBalance.subtract(totalDebit).compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Insufficient balance on account " + accountId);
            }
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