package com.djimbinov.platform.ai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation")
public class Conversation {

  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private String title;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected Conversation() {
  }

  public Conversation(
        UUID id,
        UUID userId,
        String title,
        Instant createdAt
  ) {
    this.id = id;
    this.userId = userId;
    this.title = title;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getTitle() {
    return title;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}