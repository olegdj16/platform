package com.djimbinov.platform.ai.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class OpenAiEmbeddingService implements EmbeddingService {

  private final EmbeddingModel embeddingModel;

  public OpenAiEmbeddingService(
        EmbeddingModel embeddingModel
  ) {
    this.embeddingModel = embeddingModel;
  }

  @Override
  public float[] embed(String text) {
    if (text == null || text.isBlank()) {
      throw new IllegalArgumentException(
            "Text must not be null or blank"
      );
    }

    return embeddingModel.embed(text);
  }
}