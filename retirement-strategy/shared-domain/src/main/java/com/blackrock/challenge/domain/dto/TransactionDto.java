package com.blackrock.challenge.domain.dto;

public record TransactionDto(
    String date,
    double amount,
    double ceiling,
    double remanent
) {}
