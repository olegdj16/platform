package com.djimbinov.platform.ai.rag;

import com.djimbinov.platform.ai.embedding.EmbeddingService;
import com.djimbinov.platform.document.model.DocumentChunk;
import com.djimbinov.platform.document.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class DocumentRetrievalService {

  private static final int DEFAULT_LIMIT = 5;

  private final EmbeddingService embeddingService;
  private final DocumentChunkRepository documentChunkRepository;

  public DocumentRetrievalService(
        EmbeddingService embeddingService,
        DocumentChunkRepository documentChunkRepository
  ) {
    this.embeddingService = embeddingService;
    this.documentChunkRepository = documentChunkRepository;
  }

  public List<DocumentChunk> retrieve(
        UUID projectId,
        String question
  ) {
    if (projectId == null) {
      throw new IllegalArgumentException(
            "Project id must not be null"
      );
    }

    if (question == null || question.isBlank()) {
      throw new IllegalArgumentException(
            "Question must not be null or blank"
      );
    }

    float[] embedding =
          embeddingService.embed(question);

    String vector =
          toVectorLiteral(embedding);

    return documentChunkRepository.findSimilarChunks(
          projectId,
          vector,
          DEFAULT_LIMIT
    );
  }

  private String toVectorLiteral(float[] embedding) {
    return IntStream.range(
                0,
                embedding.length
          )
          .mapToObj(index ->
                Float.toString(
                      embedding[index]
                )
          )
          .collect(
                Collectors.joining(
                      ",",
                      "[",
                      "]"
                )
          );
  }
}