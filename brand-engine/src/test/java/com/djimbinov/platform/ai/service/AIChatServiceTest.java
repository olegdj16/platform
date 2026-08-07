package com.djimbinov.platform.ai.service;

import com.djimbinov.platform.ai.provider.AIProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIChatServiceTest {

  @Mock
  private AIProvider aiProvider;

  private AIChatService aiChatService;

  @BeforeEach
  void setUp() {
    aiChatService = new AIChatService(aiProvider);
  }

  @Test
  void chatShouldReturnProviderResponse() {
    String message = "Hello AI";
    String providerResponse = "AI response for: Hello AI";

    when(aiProvider.chat(message))
          .thenReturn(providerResponse);

    String result = aiChatService.chat(message);

    assertEquals(providerResponse, result);

    verify(aiProvider).chat(message);
    verifyNoMoreInteractions(aiProvider);
  }
}