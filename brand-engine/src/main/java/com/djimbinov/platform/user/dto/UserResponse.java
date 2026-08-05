package com.djimbinov.platform.user.dto;

import com.djimbinov.platform.user.model.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
      UUID id,
      String email,
      UserRole role,
      Instant createdAt,
      Instant updatedAt
) {
}