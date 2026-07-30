package com.king.ledgerengine.domain.transaction.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateTransactionDto {
    @NotBlank
    private String description;

    @NotEmpty
    @Valid
    private List<EntryLineDto> entries;
}