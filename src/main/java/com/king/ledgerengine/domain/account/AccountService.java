package com.king.ledgerengine.domain.account;

import com.king.ledgerengine.domain.account.dto.CreateAccountDto;
import com.king.ledgerengine.domain.account.entity.Account;
import com.king.ledgerengine.domain.entry.EntryRepository;
import com.king.ledgerengine.domain.entry.entity.Entry;
import com.king.ledgerengine.domain.user.UserRepository;
import com.king.ledgerengine.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final EntryRepository entryRepository;
    private final UserRepository userRepository;

    // POST
    public Account create(@NonNull CreateAccountDto payload, String userId){
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Account account = Account.builder()
                .name(payload.getName())
                .type(payload.getType())
                .user(userRepository.getReferenceById(userId))
                .build();

        return accountRepository.save(account);
    }

    // GET
    public List<Account> getAll(String userId) {
        return accountRepository.findByUserId(userId);
    }

    // GET
    public Account getOne(String userId, String id) {
        return accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found or does not belong to this user"));
    }

    // GET
    public BigDecimal getBalance(String accountId, String userId) {
        assertOwnership(userId, accountId);
        return entryRepository.getBalance(accountId);
    }

    // GET
    public List<Entry> getEntries(String accountId, String userId) {
        assertOwnership(userId, accountId);
        return entryRepository.findByAccountId(accountId);
    }

    // helper
    private void assertOwnership(String userId, String accountId){
        accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found or does not belong to this user"));
        log.info("account {} is owned by user {}", accountId, userId);
    }
}