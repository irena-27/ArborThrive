package com.blackrock.challenge.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PPeriodDto(
    @NotNull Double extra,
    @NotBlank String start,
    @NotBlank String end
) {}
