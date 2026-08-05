package com.djimbinov.platform.document.storage;

public record StoredFile(
      String originalFilename,
      String contentType,
      String storageKey,
      long sizeBytes
) {
}