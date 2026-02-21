package com.blackrock.challenge.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record KPeriodDto(
    @NotBlank String start,
    @NotBlank String end
) {}
