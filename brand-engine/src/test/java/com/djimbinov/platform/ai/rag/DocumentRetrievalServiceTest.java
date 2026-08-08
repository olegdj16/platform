package com.djimbinov.platform.ai.rag;

import com.djimbinov.platform.ai.embedding.EmbeddingService;
import com.djimbinov.platform.document.model.DocumentChunk;
import com.djimbinov.platform.document.repository.DocumentChunkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DocumentRetrievalServiceTest {

  private EmbeddingService embeddingService;
  private DocumentChunkRepository documentChunkRepository;
  private DocumentRetrievalService documentRetrievalService;

  @BeforeEach
  void setUp() {
    embeddingService = mock(EmbeddingService.class);
    documentChunkRepository = mock(DocumentChunkRepository.class);

    documentRetrievalService =
          new DocumentRetrievalService(
                embeddingService,
                documentChunkRepository
          );
  }

  @Test
  void retrieve_shouldEmbedQuestionAndReturnSimilarChunks() {
    UUID projectId = UUID.randomUUID();

    float[] embedding =
          new float[]{0.1f, 0.2f, 0.3f};

    List<DocumentChunk> expected =
          List.of(
                mock(DocumentChunk.class),
                mock(DocumentChunk.class)
          );

    when(embeddingService.embed("How is authentication implemented?"))
          .thenReturn(embedding);

    when(documentChunkRepository.findSimilarChunks(
          projectId,
          "[0.1,0.2,0.3]",
          5
    )).thenReturn(expected);

    List<DocumentChunk> result =
          documentRetrievalService.retrieve(
                projectId,
                "How is authentication implemented?"
          );

    assertEquals(expected, result);

    verify(embeddingService)
          .embed("How is authentication implemented?");

    verify(documentChunkRepository)
          .findSimilarChunks(
                projectId,
                "[0.1,0.2,0.3]",
                5
          );
  }

  @Test
  void retrieve_shouldRejectNullProjectId() {
    assertThrows(
          IllegalArgumentException.class,
          () -> documentRetrievalService.retrieve(
                null,
                "Question"
          )
    );

    verifyNoInteractions(
          embeddingService,
          documentChunkRepository
    );
  }

  @Test
  void retrieve_shouldRejectBlankQuestion() {
    UUID projectId = UUID.randomUUID();

    assertThrows(
          IllegalArgumentException.class,
          () -> documentRetrievalService.retrieve(
                projectId,
                "   "
          )
    );

    verifyNoInteractions(
          embeddingService,
          documentChunkRepository
    );
  }
}