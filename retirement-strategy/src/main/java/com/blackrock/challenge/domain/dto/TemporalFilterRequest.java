package com.blackrock.challenge.domain.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TemporalFilterRequest(
    List<QPeriodDto> q,
    List<PPeriodDto> p,
    List<KPeriodDto> k,
    @NotNull List<TransactionDto> transactions
) {}
