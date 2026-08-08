package com.djimbinov.platform.ai.repository;

import com.djimbinov.platform.ai.model.ChatMessageEntity;
import com.djimbinov.platform.ai.model.Conversation;
import com.djimbinov.platform.ai.model.MessageRole;
import com.djimbinov.platform.user.model.User;
import com.djimbinov.platform.user.model.UserRole;
import com.djimbinov.platform.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(
      replace = AutoConfigureTestDatabase.Replace.NONE
)
class ChatMessageRepositoryTest {

  @Autowired
  private ChatMessageRepository chatMessageRepository;

  @Autowired
  private ConversationRepository conversationRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  void shouldSaveChatMessage() {

    Instant now = Instant.now();

    User user = new User(
          UUID.randomUUID(),
          "chat-test-1@example.com",
          "password-hash",
          UserRole.USER,
          now,
          now
    );

    userRepository.save(user);

    UUID conversationId = UUID.randomUUID();

    Conversation conversation = new Conversation(
          conversationId,
          user.getId(),
          "Memory Test",
          now
    );

    conversationRepository.save(conversation);

    ChatMessageEntity message =
          new ChatMessageEntity(
                UUID.randomUUID(),
                conversationId,
                MessageRole.USER,
                "What technologies does Brand Engine use?",
                now
          );

    ChatMessageEntity saved =
          chatMessageRepository.save(message);

    assertEquals(conversationId, saved.getConversationId());
    assertEquals(MessageRole.USER, saved.getRole());
    assertEquals(
          "What technologies does Brand Engine use?",
          saved.getContent()
    );
  }

  @Test
  void shouldFindMessagesInChronologicalOrder() {

    Instant now = Instant.now();

    User user = new User(
          UUID.randomUUID(),
          "chat-test-2@example.com",
          "password-hash",
          UserRole.USER,
          now,
          now
    );

    userRepository.save(user);

    UUID conversationId = UUID.randomUUID();

    Conversation conversation = new Conversation(
          conversationId,
          user.getId(),
          "Ordered Conversation",
          now
    );

    conversationRepository.save(conversation);

    Instant firstTime = now;
    Instant secondTime = now.plusSeconds(5);

    ChatMessageEntity secondMessage =
          new ChatMessageEntity(
                UUID.randomUUID(),
                conversationId,
                MessageRole.ASSISTANT,
                "Second message",
                secondTime
          );

    ChatMessageEntity firstMessage =
          new ChatMessageEntity(
                UUID.randomUUID(),
                conversationId,
                MessageRole.USER,
                "First message",
                firstTime
          );

    chatMessageRepository.save(secondMessage);
    chatMessageRepository.save(firstMessage);

    List<ChatMessageEntity> messages =
          chatMessageRepository
                .findByConversationIdOrderByCreatedAtAsc(
                      conversationId
                );

    assertEquals(2, messages.size());
    assertEquals("First message", messages.get(0).getContent());
    assertEquals("Second message", messages.get(1).getContent());
  }
}