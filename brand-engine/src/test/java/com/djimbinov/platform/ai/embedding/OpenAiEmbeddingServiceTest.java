package com.djimbinov.platform.ai.embedding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpenAiEmbeddingServiceTest {

  private EmbeddingModel embeddingModel;
  private OpenAiEmbeddingService embeddingService;

  @BeforeEach
  void setUp() {
    embeddingModel = mock(EmbeddingModel.class);
    embeddingService =
          new OpenAiEmbeddingService(embeddingModel);
  }

  @Test
  void embed_shouldReturnEmbedding() {
    float[] expected =
          new float[]{0.1f, 0.2f, 0.3f};

    when(embeddingModel.embed("Brand Engine"))
          .thenReturn(expected);

    float[] result =
          embeddingService.embed("Brand Engine");

    assertArrayEquals(expected, result);

    verify(embeddingModel)
          .embed("Brand Engine");
  }

  @Test
  void embed_shouldRejectNullText() {
    assertThrows(
          IllegalArgumentException.class,
          () -> embeddingService.embed(null)
    );

    verifyNoInteractions(embeddingModel);
  }

  @Test
  void embed_shouldRejectBlankText() {
    assertThrows(
          IllegalArgumentException.class,
          () -> embeddingService.embed("   ")
    );

    verifyNoInteractions(embeddingModel);
  }
}