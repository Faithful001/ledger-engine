package com.king.ledgerengine.domain.account;

import com.king.ledgerengine.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByIdAndUserId(
            @Param("accountId") String accountId,
            @Param("userId") String userId
    );
    List<Account> findByUserId(
            @Param("userId") String userId
    );
}
