package com.djimbinov.platform.ai.service;

import com.djimbinov.platform.ai.provider.AIProvider;
import com.djimbinov.platform.ai.rag.DocumentRetrievalService;
import com.djimbinov.platform.ai.rag.RagPromptBuilder;
import com.djimbinov.platform.document.model.DocumentChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentAIServiceTest {

  @Mock
  private DocumentRetrievalService documentRetrievalService;

  @Mock
  private RagPromptBuilder ragPromptBuilder;

  @Mock
  private AIProvider aiProvider;

  private DocumentAIService documentAIService;

  @BeforeEach
  void setUp() {
    documentAIService =
          new DocumentAIService(
                documentRetrievalService,
                ragPromptBuilder,
                aiProvider
          );
  }

  @Test
  void shouldAnswerQuestionUsingRetrievedChunks() {
    UUID projectId = UUID.randomUUID();

    String question =
          "What technologies are used?";

    DocumentChunk firstChunk =
          mock(DocumentChunk.class);

    DocumentChunk secondChunk =
          mock(DocumentChunk.class);

    List<DocumentChunk> chunks =
          List.of(
                firstChunk,
                secondChunk
          );

    String prompt =
          "RAG prompt with relevant document context";

    String expectedResponse =
          "Brand Engine uses Java, Spring Boot and PostgreSQL.";

    when(documentRetrievalService.retrieve(
          projectId,
          question
    )).thenReturn(chunks);

    when(ragPromptBuilder.build(
          question,
          chunks
    )).thenReturn(prompt);

    when(aiProvider.chat(prompt))
          .thenReturn(expectedResponse);

    String result =
          documentAIService.ask(
                projectId,
                question
          );

    assertEquals(
          expectedResponse,
          result
    );

    verify(documentRetrievalService)
          .retrieve(
                projectId,
                question
          );

    verify(ragPromptBuilder)
          .build(
                question,
                chunks
          );

    verify(aiProvider)
          .chat(prompt);
  }

  @Test
  void shouldRejectNullProjectId() {
    assertThrows(
          IllegalArgumentException.class,
          () -> documentAIService.ask(
                null,
                "Question"
          )
    );

    verifyNoInteractions(
          documentRetrievalService,
          ragPromptBuilder,
          aiProvider
    );
  }

  @Test
  void shouldRejectBlankQuestion() {
    UUID projectId =
          UUID.randomUUID();

    assertThrows(
          IllegalArgumentException.class,
          () -> documentAIService.ask(
                projectId,
                "   "
          )
    );

    verifyNoInteractions(
          documentRetrievalService,
          ragPromptBuilder,
          aiProvider
    );
  }
}