package com.karan.simplejwt1.domain;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record RegisterRequest(
       @NonNull String username,
       @NonNull String password,
       String email
) {
}
