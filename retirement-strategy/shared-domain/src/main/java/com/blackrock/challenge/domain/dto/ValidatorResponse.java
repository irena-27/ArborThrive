package com.blackrock.challenge.domain.dto;

import java.util.List;

public record ValidatorResponse(
    List<TransactionDto> valid,
    List<InvalidTransactionDto> invalid
) {}
