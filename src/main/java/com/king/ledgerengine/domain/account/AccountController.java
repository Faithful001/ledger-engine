package com.king.ledgerengine.domain.account;

import com.king.ledgerengine.domain.account.dto.CreateAccountDto;
import com.king.ledgerengine.domain.account.entity.Account;
import com.king.ledgerengine.domain.entry.entity.Entry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Create accounts and query balances/entry history")
public class AccountController {
    private final AccountService accountService;

    @Operation(summary = "Create a new account")
    @PostMapping
    public ResponseEntity<Account> create(@AuthenticationPrincipal String userId, @Valid @RequestBody CreateAccountDto payload) {
        Account account = accountService.create(payload, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{id}/balance")
    public Map<String, BigDecimal> getBalance(@AuthenticationPrincipal String userId, @PathVariable String id) {
        BigDecimal balance = accountService.getBalance(id, userId);
        return Map.of("balance", balance);
    }

    @Operation(summary = "Get the full entry history for an account")
    @GetMapping("/{id}/entries")
    public List<Entry> getEntries(@AuthenticationPrincipal String userId, @PathVariable String id) {
        return accountService.getEntries(id, userId);
    }
}