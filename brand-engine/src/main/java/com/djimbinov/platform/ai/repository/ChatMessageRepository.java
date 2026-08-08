package com.djimbinov.platform.ai.repository;

import com.djimbinov.platform.ai.model.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository
      extends JpaRepository<ChatMessageEntity, UUID> {

  List<ChatMessageEntity>
  findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}