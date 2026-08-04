package com.djimbinov.platform.document.model;

import com.djimbinov.platform.project.model.Project;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document {

  @Id
  private UUID id;

  @Column(nullable = false, length = 150)
  private String name;

  @Column(name = "original_filename", nullable = false, length = 255)
  private String originalFilename;

  @Column(name = "content_type", length = 150)
  private String contentType;

  @Column(name = "storage_key", length = 500)
  private String storageKey;

  @Column(name = "size_bytes")
  private Long sizeBytes;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  protected Document() {
  }

  public Document(
        UUID id,
        String name,
        String originalFilename,
        String contentType,
        String storageKey,
        Long sizeBytes,
        Instant createdAt,
        Project project
  ) {
    this.id = id;
    this.name = name;
    this.originalFilename = originalFilename;
    this.contentType = contentType;
    this.storageKey = storageKey;
    this.sizeBytes = sizeBytes;
    this.createdAt = createdAt;
    this.project = project;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getOriginalFilename() {
    return originalFilename;
  }

  public String getContentType() {
    return contentType;
  }

  public String getStorageKey() {
    return storageKey;
  }

  public Long getSizeBytes() {
    return sizeBytes;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Project getProject() {
    return project;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setProject(Project project) {
    this.project = project;
  }
}