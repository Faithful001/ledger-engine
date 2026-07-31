package com.king.ledgerengine.domain.account.dto;

import com.king.ledgerengine.domain.account.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateAccountDto {
    @Schema(description = "Account name", example = "Customer Cash Account")
    @NotBlank
    private String name;

    @Schema(description = "Type of account", example = "ASSET")
    @NotNull
    private AccountType type;

//    @Schema(description = "ISO 4217 currency code", example = "NGN")
//    @NotBlank
//    private String currency;
}