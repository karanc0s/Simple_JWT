package com.karan.simplejwt1.domain;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UserDataResponse(
        String username,
        String email,
        Instant lastUpdatedAt
) {
}
