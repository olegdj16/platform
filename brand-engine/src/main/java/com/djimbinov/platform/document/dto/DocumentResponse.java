package com.djimbinov.platform.document.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
      UUID id,
      UUID projectId,
      String name,
      String originalFilename,
      String contentType,
      String storageKey,
      Long sizeBytes,
      Instant createdAt
) {
}