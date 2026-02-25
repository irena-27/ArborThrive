package com.blackrock.challenge.domain.dto;

public record InvalidTransactionDto(
    String date,
    double amount,
    double ceiling,
    double remanent,
    String message
) {}
