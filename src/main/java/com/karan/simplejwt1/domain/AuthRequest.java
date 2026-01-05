package com.karan.simplejwt1.domain;

import lombok.Builder;

@Builder
public record AuthRequest(
        String username,
        String password
) {
}
