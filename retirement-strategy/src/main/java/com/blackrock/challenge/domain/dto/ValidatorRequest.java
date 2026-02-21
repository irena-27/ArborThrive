package com.blackrock.challenge.domain.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ValidatorRequest(
    @NotNull Double wage,
    @NotNull List<TransactionDto> transactions
) {}
