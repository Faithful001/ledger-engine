package com.king.ledgerengine.domain.account.dto;

import com.king.ledgerengine.domain.account.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateAccountDto {
    @NotNull
    private String name;

    @NotNull
    private AccountType type;
}
