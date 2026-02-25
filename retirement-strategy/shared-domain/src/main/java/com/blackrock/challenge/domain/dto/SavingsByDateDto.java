package com.blackrock.challenge.domain.dto;

public record SavingsByDateDto(
    String start,
    String end,
    double amount,
    double profits,
    double taxBenefit
) {}
