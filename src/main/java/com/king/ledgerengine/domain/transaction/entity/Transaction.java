package com.king.ledgerengine.domain.transaction.entity;

import com.king.ledgerengine.domain.entry.entity.Entry;
import com.king.ledgerengine.domain.transaction.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Builder.Default
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Entry> entries = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * A user is "involved" in this transaction if they own at least one
     * of the accounts referenced by its entries.
     */
    public boolean involvesUser(String userId) {
        return entries.stream()
                .anyMatch(entry -> entry.getAccount().getUser().getId().equals(userId));
    }
}