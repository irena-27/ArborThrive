package com.blackrock.challenge.domain.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReturnsRequest(
    @NotNull Integer age,
    @NotNull Double wage,
    @NotNull Double inflation,
    List<QPeriodDto> q,
    List<PPeriodDto> p,
    List<KPeriodDto> k,
    @NotNull List<ExpenseDto> transactions
) {}
