package com.blackrock.challenge.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QPeriodDto(
    @NotNull Double fixed,
    @NotBlank String start,
    @NotBlank String end
) {}
