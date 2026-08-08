package com.djimbinov.platform.document.repository;

import com.djimbinov.platform.document.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentChunkRepository
      extends JpaRepository<DocumentChunk, UUID> {

  List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(UUID documentId);

  void deleteByDocumentId(UUID documentId);
}