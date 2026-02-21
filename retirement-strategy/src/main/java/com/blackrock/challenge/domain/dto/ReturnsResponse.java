package com.blackrock.challenge.domain.dto;

import java.util.List;

public record ReturnsResponse(
    double transactionsTotalAmount,
    double transactionsTotalCeiling,
    List<SavingsByDateDto> savingsByDates
) {}
