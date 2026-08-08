package com.djimbinov.platform.ai.repository;

import com.djimbinov.platform.ai.model.Conversation;
import com.djimbinov.platform.user.model.User;
import com.djimbinov.platform.user.model.UserRole;
import com.djimbinov.platform.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
      replace = AutoConfigureTestDatabase.Replace.NONE
)
class ConversationRepositoryTest {

  @Autowired
  private ConversationRepository conversationRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  void shouldSaveConversation() {

    Instant now = Instant.now();

    User user = new User(
          UUID.randomUUID(),
          "conversation-test-1@example.com",
          "password-hash",
          UserRole.USER,
          now,
          now
    );

    userRepository.save(user);

    UUID id = UUID.randomUUID();

    Conversation conversation = new Conversation(
          id,
          user.getId(),
          "Brand Engine Discussion",
          now
    );

    Conversation saved =
          conversationRepository.save(conversation);

    assertNotNull(saved);
    assertEquals(id, saved.getId());
    assertEquals(user.getId(), saved.getUserId());
    assertEquals(
          "Brand Engine Discussion",
          saved.getTitle()
    );
    assertEquals(now, saved.getCreatedAt());
  }

  @Test
  void shouldFindConversationById() {

    Instant now = Instant.now();

    User user = new User(
          UUID.randomUUID(),
          "conversation-test-2@example.com",
          "password-hash",
          UserRole.USER,
          now,
          now
    );

    userRepository.save(user);

    UUID id = UUID.randomUUID();

    Conversation conversation = new Conversation(
          id,
          user.getId(),
          "AI Architecture Discussion",
          now
    );

    conversationRepository.save(conversation);

    Optional<Conversation> result =
          conversationRepository.findById(id);

    assertTrue(result.isPresent());
    assertEquals(id, result.get().getId());
    assertEquals(user.getId(), result.get().getUserId());
    assertEquals(
          "AI Architecture Discussion",
          result.get().getTitle()
    );
  }
}