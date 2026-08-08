package com.djimbinov.platform.ai.service;

import com.djimbinov.platform.ai.exception.ConversationNotFoundException;
import com.djimbinov.platform.ai.model.ChatMessageEntity;
import com.djimbinov.platform.ai.model.MessageRole;
import com.djimbinov.platform.ai.repository.ChatMessageRepository;
import com.djimbinov.platform.ai.repository.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ChatMessageService {

  private final ChatMessageRepository chatMessageRepository;
  private final ConversationRepository conversationRepository;

  public ChatMessageService(
        ChatMessageRepository chatMessageRepository,
        ConversationRepository conversationRepository
  ) {
    this.chatMessageRepository = chatMessageRepository;
    this.conversationRepository = conversationRepository;
  }

  @Transactional
  public ChatMessageEntity saveUserMessage(
        UUID conversationId,
        String content
  ) {

    return saveMessage(
          conversationId,
          MessageRole.USER,
          content
    );
  }

  @Transactional
  public ChatMessageEntity saveAssistantMessage(
        UUID conversationId,
        String content
  ) {

    return saveMessage(
          conversationId,
          MessageRole.ASSISTANT,
          content
    );
  }

  @Transactional(readOnly = true)
  public List<ChatMessageEntity> getConversationMessages(
        UUID conversationId
  ) {

    verifyConversationExists(conversationId);

    return chatMessageRepository
          .findByConversationIdOrderByCreatedAtAsc(
                conversationId
          );
  }

  private ChatMessageEntity saveMessage(
        UUID conversationId,
        MessageRole role,
        String content
  ) {

    verifyConversationExists(conversationId);

    ChatMessageEntity message =
          new ChatMessageEntity(
                UUID.randomUUID(),
                conversationId,
                role,
                content,
                Instant.now()
          );

    return chatMessageRepository.save(message);
  }

  private void verifyConversationExists(
        UUID conversationId
  ) {

    if (!conversationRepository.existsById(conversationId)) {
      throw new ConversationNotFoundException(conversationId);
    }
  }
}