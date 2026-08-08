package com.djimbinov.platform.ai.mapper;

import com.djimbinov.platform.ai.dto.ConversationResponse;
import com.djimbinov.platform.ai.model.Conversation;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ConversationMapper {

  public Conversation toEntity(
        UUID userId,
        String title
  ) {

    return new Conversation(
          UUID.randomUUID(),
          userId,
          title,
          Instant.now()
    );
  }

  public ConversationResponse toResponse(
        Conversation conversation
  ) {

    return new ConversationResponse(
          conversation.getId(),
          conversation.getTitle(),
          conversation.getCreatedAt()
    );
  }
}