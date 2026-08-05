package com.djimbinov.platform.auth.dto;

import java.util.UUID;

public record AuthResponse(
      String token,
      String tokenType,
      UUID userId,
      String email
) {
}