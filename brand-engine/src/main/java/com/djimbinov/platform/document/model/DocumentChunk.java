package com.djimbinov.platform.document.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
        name = "document_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_document_chunks_document")
  )
  private Document document;

  @Column(name = "chunk_index", nullable = false)
  private Integer chunkIndex;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @JdbcTypeCode(SqlTypes.VECTOR)
  @Array(length = 1536)
  @Column(name = "embedding", columnDefinition = "vector(1536)")
  private float[] embedding;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected DocumentChunk() {
  }

  public DocumentChunk(
        UUID id,
        Document document,
        Integer chunkIndex,
        String content,
        float[] embedding,
        Instant createdAt
  ) {
    this.id = id;
    this.document = document;
    this.chunkIndex = chunkIndex;
    this.content = content;
    this.embedding = embedding;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public Document getDocument() {
    return document;
  }

  public Integer getChunkIndex() {
    return chunkIndex;
  }

  public String getContent() {
    return content;
  }

  public float[] getEmbedding() {
    return embedding;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}