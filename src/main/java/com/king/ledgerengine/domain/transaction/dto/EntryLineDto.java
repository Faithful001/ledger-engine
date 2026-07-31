package com.king.ledgerengine.domain.transaction.dto;

import com.king.ledgerengine.domain.entry.enums.EntryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntryLineDto {
    @Schema(description = "Account to debit or credit", example = "a1b2c3d4-...")
    @NotNull
    private String accountId;

    @Schema(description = "Amount for this entry line", example = "100.00")
    @NotNull
    @Positive
    private java.math.BigDecimal amount;

    @Schema(description = "Whether this entry is a debit or credit", example = "DEBIT")
    @NotNull
    private EntryType type;
}