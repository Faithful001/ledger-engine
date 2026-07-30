package com.king.ledgerengine.domain.entry;

import com.king.ledgerengine.domain.entry.entity.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface EntryRepository extends JpaRepository<Entry, String> {
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN e.type = 'CREDIT' THEN e.amount ELSE -e.amount END), 0)
        FROM Entry e
        WHERE e.account.id = :accountId
    """)
    BigDecimal getBalance(@Param("accountId") String accountId);

    List<Entry> findByAccountId(@Param("accountId") String accountId);
}