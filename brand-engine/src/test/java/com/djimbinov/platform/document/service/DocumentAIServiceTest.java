package com.djimbinov.platform.ai.service;

import com.djimbinov.platform.ai.provider.AIProvider;
import com.djimbinov.platform.document.exception.DocumentNotFoundException;
import com.djimbinov.platform.document.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentAIServiceTest {

  @Mock
  private DocumentService documentService;

  @Mock
  private AIProvider aiProvider;

  private DocumentAIService documentAIService;

  @BeforeEach
  void setUp() {
    documentAIService = new DocumentAIService(
          documentService,
          aiProvider
    );
  }

  @Test
  void shouldAnswerQuestionUsingDocument() {

    UUID documentId = UUID.randomUUID();

    when(documentService.extractText(documentId))
          .thenReturn(
                "Brand Engine uses Java, Spring Boot and PostgreSQL."
          );

    when(aiProvider.chat(any()))
          .thenReturn(
                "Brand Engine uses Java, Spring Boot and PostgreSQL."
          );

    String result = documentAIService.ask(
          documentId,
          "What technologies are used?"
    );

    assertEquals(
          "Brand Engine uses Java, Spring Boot and PostgreSQL.",
          result
    );

    verify(documentService)
          .extractText(documentId);

    verify(aiProvider)
          .chat(any());
  }

  @Test
  void shouldPropagateDocumentNotFoundException() {

    UUID documentId = UUID.randomUUID();

    when(documentService.extractText(documentId))
          .thenThrow(new DocumentNotFoundException(documentId));

    assertThrows(
          DocumentNotFoundException.class,
          () -> documentAIService.ask(
                documentId,
                "What is this document about?"
          )
    );

    verify(documentService).extractText(documentId);
    verifyNoInteractions(aiProvider);
  }
}