package com.king.ledgerengine.domain.transaction.dto;

import com.king.ledgerengine.domain.entry.enums.EntryType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntryLineDto {
    @NotNull
    private String accountId;

    @NotNull
    @Positive
    private java.math.BigDecimal amount;

    @NotNull
    private EntryType type;
}