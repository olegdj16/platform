package com.djimbinov.platform.ai.repository;

import com.djimbinov.platform.ai.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConversationRepository
      extends JpaRepository<Conversation, UUID> {
}