package com.djimbinov.platform.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DocumentRequest(

      @NotNull
      UUID projectId,

      @NotBlank
      String name,

      @NotBlank
      String originalFilename,

      String contentType,

      String storageKey,

      Long sizeBytes
) {
}