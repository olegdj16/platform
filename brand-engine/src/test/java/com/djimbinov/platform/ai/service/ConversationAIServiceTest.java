package com.djimbinov.platform.ai.service;

import com.djimbinov.platform.ai.model.ChatMessageEntity;
import com.djimbinov.platform.ai.model.MessageRole;
import com.djimbinov.platform.ai.provider.AIProvider;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationAIServiceTest {

  @Mock
  private ChatMessageService chatMessageService;

  @Mock
  private AIProvider aiProvider;

  private ConversationAIService conversationAIService;

  @BeforeEach
  void setUp() {
    conversationAIService = new ConversationAIService(
          chatMessageService,
          aiProvider
    );
  }

  @Test
  void shouldUseConversationHistoryAndSaveAssistantResponse() {

    UUID conversationId = UUID.randomUUID();

    ChatMessageEntity firstUserMessage =
          new ChatMessageEntity(
                UUID.randomUUID(),
                conversationId,
                MessageRole.USER,
                "What database does Brand Engine use?",
                Instant.now()
          );

    ChatMessageEntity firstAssistantMessage =
          new ChatMessageEntity(
                UUID.randomUUID(),
                conversationId,
                MessageRole.ASSISTANT,
                "Brand Engine uses PostgreSQL.",
                Instant.now().plusSeconds(1)
          );

    ChatMessageEntity currentUserMessage =
          new ChatMessageEntity(
                UUID.randomUUID(),
                conversationId,
                MessageRole.USER,
                "What did you say the database was?",
                Instant.now().plusSeconds(2)
          );

    when(chatMessageService.getConversationMessages(conversationId))
          .thenReturn(
                List.of(
                      firstUserMessage,
                      firstAssistantMessage,
                      currentUserMessage
                )
          );

    when(aiProvider.chat(anyString()))
          .thenReturn("PostgreSQL.");

    String result = conversationAIService.chat(
          conversationId,
          "What did you say the database was?"
    );

    assertEquals("PostgreSQL.", result);

    verify(chatMessageService).saveUserMessage(
          conversationId,
          "What did you say the database was?"
    );

    verify(chatMessageService)
          .getConversationMessages(conversationId);

    ArgumentCaptor<String> promptCaptor =
          ArgumentCaptor.forClass(String.class);

    verify(aiProvider).chat(promptCaptor.capture());

    String prompt = promptCaptor.getValue();

    assertEquals(true, prompt.contains(
          "USER: What database does Brand Engine use?"
    ));

    assertEquals(true, prompt.contains(
          "ASSISTANT: Brand Engine uses PostgreSQL."
    ));

    assertEquals(true, prompt.contains(
          "USER: What did you say the database was?"
    ));

    verify(chatMessageService).saveAssistantMessage(
          conversationId,
          "PostgreSQL."
    );
  }

  @Test
  void shouldNotSaveAssistantMessageWhenAiProviderFails() {

    UUID conversationId = UUID.randomUUID();

    ChatMessageEntity currentUserMessage =
          new ChatMessageEntity(
                UUID.randomUUID(),
                conversationId,
                MessageRole.USER,
                "Hello",
                Instant.now()
          );

    when(chatMessageService.getConversationMessages(conversationId))
          .thenReturn(List.of(currentUserMessage));

    when(aiProvider.chat(anyString()))
          .thenThrow(new RuntimeException("AI failure"));

    org.junit.jupiter.api.Assertions.assertThrows(
          RuntimeException.class,
          () -> conversationAIService.chat(
                conversationId,
                "Hello"
          )
    );

    verify(chatMessageService).saveUserMessage(
          conversationId,
          "Hello"
    );

    verify(chatMessageService, never())
          .saveAssistantMessage(any(), anyString());
  }
}