package com.djimbinov.platform.document.repository;

import com.djimbinov.platform.document.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentChunkRepository
      extends JpaRepository<DocumentChunk, UUID> {

  List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(UUID documentId);

  void deleteByDocumentId(UUID documentId);

  @Query(
        value = """
              SELECT dc.*
              FROM document_chunks dc
              JOIN documents d
                ON dc.document_id = d.id
              WHERE d.project_id = :projectId
                AND dc.embedding IS NOT NULL
              ORDER BY dc.embedding <=> CAST(:queryEmbedding AS vector)
              LIMIT :limit
              """,
        nativeQuery = true
  )
  List<DocumentChunk> findSimilarChunks(
        @Param("projectId") UUID projectId,
        @Param("queryEmbedding") String queryEmbedding,
        @Param("limit") int limit
  );
}