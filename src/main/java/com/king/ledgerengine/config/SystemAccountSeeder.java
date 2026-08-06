package com.king.ledgerengine.config;

import com.king.ledgerengine.domain.account.AccountRepository;
import com.king.ledgerengine.domain.account.entity.Account;
import com.king.ledgerengine.domain.account.enums.AccountOwnerType;
import com.king.ledgerengine.domain.account.enums.AccountType;
import com.king.ledgerengine.domain.user.UserRepository;
import com.king.ledgerengine.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SystemAccountSeeder implements CommandLineRunner {

    private static final String SYSTEM_USER_EMAIL = "system@ledger-engine.internal";
    private static final String SYSTEM_ACCOUNT_NAME = "System Deposit Account";

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {
        User systemUser = userRepository.findByEmail(SYSTEM_USER_EMAIL)
                .orElseGet(() -> {
                    User user = User.builder()
                            .firstName("System")
                            .lastName("Account")
                            .email(SYSTEM_USER_EMAIL)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .build();
                    return userRepository.save(user);
                });

        boolean hasSystemAccount = accountRepository.findByUserId(systemUser.getId())
                .stream()
                .anyMatch(account -> account.getOwnerType() == AccountOwnerType.SYSTEM);

        if (!hasSystemAccount) {
            Account systemAccount = Account.builder()
                    .name(SYSTEM_ACCOUNT_NAME)
                    .ownerType(AccountOwnerType.SYSTEM)
                    .type(AccountType.ASSET)
                    .user(systemUser)
                    .build();
            accountRepository.save(systemAccount);
        }
    }
}