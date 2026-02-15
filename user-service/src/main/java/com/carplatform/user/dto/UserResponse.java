package com.carplatform.user.dto;

import com.carplatform.user.model.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID userId,
        String name,
        String email,
        String phone,
        UserRole role,
        boolean active,
        Instant createdAt,
        Instant lastUpdated) {
}
