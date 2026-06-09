package com.karan.simplejwt1.domain;

import lombok.Builder;
import lombok.ToString;

@Builder
public record AuthRequest(
        String username,
        String password
) {
}
