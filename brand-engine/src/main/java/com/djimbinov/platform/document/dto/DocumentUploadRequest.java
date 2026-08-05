package com.djimbinov.platform.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record DocumentUploadRequest(

      @NotNull
      UUID projectId,

      @NotBlank
      String name,

      @NotNull
      MultipartFile file
) {
}