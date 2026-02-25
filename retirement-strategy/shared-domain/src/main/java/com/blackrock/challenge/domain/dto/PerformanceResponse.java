package com.blackrock.challenge.domain.dto;

public record PerformanceResponse(
    String time,
    String memory,
    int threads
) {}
