package com.king.ledgerengine.domain.account;

import com.king.ledgerengine.domain.account.entity.Account;
import com.king.ledgerengine.domain.account.enums.AccountOwnerType;
import com.king.ledgerengine.domain.account.enums.AccountType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
    SELECT a FROM Account a WHERE a.id = :id
"""
    )
    Optional<Account> findByIdWithLock(@Param("id") String id);

    Optional<Account> findFirstByOwnerType(AccountOwnerType type);
}
