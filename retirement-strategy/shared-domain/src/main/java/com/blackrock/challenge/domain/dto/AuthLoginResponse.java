package com.blackrock.challenge.domain.dto;

public record AuthLoginResponse(
    String accessToken,
    String tokenType,
    long expiresInSeconds
) {}
