package com.king.ledgerengine.domain.entry.entity;

import com.king.ledgerengine.domain.account.entity.Account;
import com.king.ledgerengine.domain.entry.enums.EntryType;
import com.king.ledgerengine.domain.transaction.entity.Transaction;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(
        name = "entries",
        indexes = {
                @Index(name = "idx_entry_account_id", columnList = "account_id")
        })
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Entry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @JoinColumn(name = "transaction_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Transaction transaction;

    @JoinColumn(name = "account_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Account account;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType type;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}