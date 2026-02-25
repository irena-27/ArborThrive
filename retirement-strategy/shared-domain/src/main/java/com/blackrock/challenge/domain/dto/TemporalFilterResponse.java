package com.blackrock.challenge.domain.dto;

import java.util.List;

public record TemporalFilterResponse(
    List<TransactionDto> valid,
    List<InvalidTransactionDto> invalid
) {}