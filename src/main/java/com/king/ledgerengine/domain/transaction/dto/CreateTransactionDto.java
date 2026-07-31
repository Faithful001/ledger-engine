package com.king.ledgerengine.domain.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateTransactionDto {
    @Schema(description = "Description for the transaction", example = "Money to mum")
    @NotBlank
    private String description;

    @Schema(description = "List of debit/credit entries for this transaction. Debits and credits must sum to equal amounts.")
    @NotEmpty
    @Valid
    private List<EntryLineDto> entries;
}