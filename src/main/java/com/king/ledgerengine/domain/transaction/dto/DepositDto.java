package com.king.ledgerengine.domain.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DepositDto {
    @Schema(description = "Account to credit", example = "a1b2c3d4-...")
    @NotBlank
    private String accountId;

    @Schema(description = "Amount to deposit", example = "50.00")
    @NotNull
    @Positive
    private BigDecimal amount;

    @Schema(description = "Description for this deposit", example = "Top up")
    @NotBlank
    private String description;
}