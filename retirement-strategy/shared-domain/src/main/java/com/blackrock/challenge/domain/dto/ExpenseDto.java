package com.blackrock.challenge.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExpenseDto(
    @NotBlank String date,
    @NotNull Double amount
) {}
