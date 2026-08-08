package com.djimbinov.platform.ai.service;

import com.djimbinov.platform.ai.exception.ConversationNotFoundException;
import com.djimbinov.platform.ai.model.ChatMessageEntity;
import com.djimbinov.platform.ai.model.MessageRole;
import com.djimbinov.platform.ai.repository.ChatMessageRepository;
import com.djimbinov.platform.ai.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

  @Mock
  private ChatMessageRepository chatMessageRepository;

  @Mock
  private ConversationRepository conversationRepository;

  private ChatMessageService chatMessageService;

  @BeforeEach
  void setUp() {
    chatMessageService = new ChatMessageService(
          chatMessageRepository,
          conversationRepository
    );
  }

  @Test
  void shouldSaveUserMessage() {

    UUID conversationId = UUID.randomUUID();

    when(conversationRepository.existsById(conversationId))
          .thenReturn(true);

    when(chatMessageRepository.save(any(ChatMessageEntity.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

    ChatMessageEntity result =
          chatMessageService.saveUserMessage(
                conversationId,
                "Hello"
          );

    assertEquals(conversationId, result.getConversationId());
    assertEquals(MessageRole.USER, result.getRole());
    assertEquals("Hello", result.getContent());

    verify(conversationRepository).existsById(conversationId);

    ArgumentCaptor<ChatMessageEntity> captor =
          ArgumentCaptor.forClass(ChatMessageEntity.class);

    verify(chatMessageRepository).save(captor.capture());

    ChatMessageEntity saved = captor.getValue();

    assertEquals(conversationId, saved.getConversationId());
    assertEquals(MessageRole.USER, saved.getRole());
    assertEquals("Hello", saved.getContent());
  }

  @Test
  void shouldSaveAssistantMessage() {

    UUID conversationId = UUID.randomUUID();

    when(conversationRepository.existsById(conversationId))
          .thenReturn(true);

    when(chatMessageRepository.save(any(ChatMessageEntity.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

    ChatMessageEntity result =
          chatMessageService.saveAssistantMessage(
                conversationId,
                "Hello from AI"
          );

    assertEquals(conversationId, result.getConversationId());
    assertEquals(MessageRole.ASSISTANT, result.getRole());
    assertEquals("Hello from AI", result.getContent());

    verify(conversationRepository).existsById(conversationId);
    verify(chatMessageRepository)
          .save(any(ChatMessageEntity.class));
  }

  @Test
  void shouldGetConversationMessages() {

    UUID conversationId = UUID.randomUUID();

    ChatMessageEntity firstMessage =
          new ChatMessageEntity(
                UUID.randomUUID(),
                conversationId,
                MessageRole.USER,
                "First message",
                Instant.now()
          );

    ChatMessageEntity secondMessage =
          new ChatMessageEntity(
                UUID.randomUUID(),
                conversationId,
                MessageRole.ASSISTANT,
                "Second message",
                Instant.now().plusSeconds(1)
          );

    List<ChatMessageEntity> expected =
          List.of(firstMessage, secondMessage);

    when(conversationRepository.existsById(conversationId))
          .thenReturn(true);

    when(chatMessageRepository
          .findByConversationIdOrderByCreatedAtAsc(conversationId))
          .thenReturn(expected);

    List<ChatMessageEntity> result =
          chatMessageService.getConversationMessages(
                conversationId
          );

    assertSame(expected, result);
    assertEquals(2, result.size());
    assertEquals("First message", result.get(0).getContent());
    assertEquals("Second message", result.get(1).getContent());

    verify(conversationRepository).existsById(conversationId);

    verify(chatMessageRepository)
          .findByConversationIdOrderByCreatedAtAsc(
                conversationId
          );
  }

  @Test
  void shouldThrowWhenSavingUserMessageForMissingConversation() {

    UUID conversationId = UUID.randomUUID();

    when(conversationRepository.existsById(conversationId))
          .thenReturn(false);

    ConversationNotFoundException exception =
          assertThrows(
                ConversationNotFoundException.class,
                () -> chatMessageService.saveUserMessage(
                      conversationId,
                      "Hello"
                )
          );

    assertEquals(
          "Conversation not found: " + conversationId,
          exception.getMessage()
    );

    verify(conversationRepository).existsById(conversationId);
    verifyNoInteractions(chatMessageRepository);
  }

  @Test
  void shouldThrowWhenGettingMessagesForMissingConversation() {

    UUID conversationId = UUID.randomUUID();

    when(conversationRepository.existsById(conversationId))
          .thenReturn(false);

    ConversationNotFoundException exception =
          assertThrows(
                ConversationNotFoundException.class,
                () -> chatMessageService
                      .getConversationMessages(conversationId)
          );

    assertEquals(
          "Conversation not found: " + conversationId,
          exception.getMessage()
    );

    verify(conversationRepository).existsById(conversationId);
    verifyNoInteractions(chatMessageRepository);
  }
}