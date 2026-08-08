package com.djimbinov.platform.document.service;

import com.djimbinov.platform.ai.embedding.EmbeddingService;
import com.djimbinov.platform.document.model.Document;
import com.djimbinov.platform.document.model.DocumentChunk;
import com.djimbinov.platform.document.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentProcessingService {

  private final PdfTextExtractor pdfTextExtractor;
  private final TextChunkingService textChunkingService;
  private final DocumentChunkRepository documentChunkRepository;
  private final EmbeddingService embeddingService;

  public DocumentProcessingService(
        PdfTextExtractor pdfTextExtractor,
        TextChunkingService textChunkingService,
        DocumentChunkRepository documentChunkRepository,
        EmbeddingService embeddingService
  ) {
    this.pdfTextExtractor = pdfTextExtractor;
    this.textChunkingService = textChunkingService;
    this.documentChunkRepository = documentChunkRepository;
    this.embeddingService = embeddingService;
  }

  @Transactional
  public List<DocumentChunk> process(
        Document document,
        Path filePath
  ) {
    if (document == null) {
      throw new IllegalArgumentException(
            "Document must not be null"
      );
    }

    if (filePath == null) {
      throw new IllegalArgumentException(
            "File path must not be null"
      );
    }

    String text =
          pdfTextExtractor.extract(filePath);

    List<String> chunks =
          textChunkingService.chunk(text);

    documentChunkRepository
          .deleteByDocumentId(document.getId());

    List<DocumentChunk> entities =
          new ArrayList<>();

    for (int index = 0; index < chunks.size(); index++) {

      String content = chunks.get(index);

      float[] embedding =
            embeddingService.embed(content);

      DocumentChunk chunk =
            new DocumentChunk(
                  UUID.randomUUID(),
                  document,
                  index,
                  content,
                  embedding,
                  Instant.now()
            );

      entities.add(chunk);
    }

    return documentChunkRepository.saveAll(entities);
  }
}