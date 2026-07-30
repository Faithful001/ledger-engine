package com.king.ledgerengine.domain.account;

import com.king.ledgerengine.domain.account.dto.CreateAccountDto;
import com.king.ledgerengine.domain.account.entity.Account;
import com.king.ledgerengine.domain.entry.entity.Entry;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> create(
            @Valid @RequestBody CreateAccountDto payload,
            @RequestHeader("X-User-Id") String userId // placeholder until real auth is wired up
    ) {
        Account account = accountService.create(payload, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{id}/balance")
    public BigDecimal getBalance(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId
    ) {
        return accountService.getBalance(id, userId);
    }

    @GetMapping("/{id}/entries")
    public List<Entry> getEntries(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId
    ) {
        return accountService.getEntries(id, userId);
    }
}