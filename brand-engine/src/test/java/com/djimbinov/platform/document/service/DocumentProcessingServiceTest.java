package com.djimbinov.platform.document.service;

import com.djimbinov.platform.document.model.Document;
import com.djimbinov.platform.document.model.DocumentChunk;
import com.djimbinov.platform.document.repository.DocumentChunkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class DocumentProcessingServiceTest {

  private PdfTextExtractor pdfTextExtractor;
  private TextChunkingService textChunkingService;
  private DocumentChunkRepository documentChunkRepository;

  private DocumentProcessingService documentProcessingService;

  @BeforeEach
  void setUp() {
    pdfTextExtractor = mock(PdfTextExtractor.class);
    textChunkingService = mock(TextChunkingService.class);
    documentChunkRepository = mock(DocumentChunkRepository.class);

    documentProcessingService =
          new DocumentProcessingService(
                pdfTextExtractor,
                textChunkingService,
                documentChunkRepository
          );
  }

  @Test
  void process_shouldExtractChunkDeleteOldAndSaveNewChunks() {
    UUID documentId = UUID.randomUUID();
    Path filePath = Path.of("uploads/test.pdf");

    Document document = mock(Document.class);

    when(document.getId())
          .thenReturn(documentId);

    when(pdfTextExtractor.extract(filePath))
          .thenReturn(
                "Extracted document text"
          );

    when(textChunkingService.chunk(
          "Extracted document text"
    )).thenReturn(
          List.of(
                "First chunk",
                "Second chunk",
                "Third chunk"
          )
    );

    when(documentChunkRepository.saveAll(anyList()))
          .thenAnswer(invocation ->
                invocation.getArgument(0)
          );

    List<DocumentChunk> result =
          documentProcessingService.process(
                document,
                filePath
          );

    verify(pdfTextExtractor)
          .extract(filePath);

    verify(textChunkingService)
          .chunk("Extracted document text");

    verify(documentChunkRepository)
          .deleteByDocumentId(documentId);

    ArgumentCaptor<List<DocumentChunk>> captor =
          ArgumentCaptor.forClass(List.class);

    verify(documentChunkRepository)
          .saveAll(captor.capture());

    List<DocumentChunk> savedChunks =
          captor.getValue();

    assertEquals(3, savedChunks.size());
    assertEquals(3, result.size());

    assertEquals(
          0,
          savedChunks.get(0).getChunkIndex()
    );

    assertEquals(
          1,
          savedChunks.get(1).getChunkIndex()
    );

    assertEquals(
          2,
          savedChunks.get(2).getChunkIndex()
    );

    assertEquals(
          "First chunk",
          savedChunks.get(0).getContent()
    );

    assertEquals(
          "Second chunk",
          savedChunks.get(1).getContent()
    );

    assertEquals(
          "Third chunk",
          savedChunks.get(2).getContent()
    );
  }

  @Test
  void process_shouldSaveEmptyListWhenNoChunksAreProduced() {
    UUID documentId = UUID.randomUUID();
    Path filePath = Path.of("uploads/empty.pdf");

    Document document = mock(Document.class);

    when(document.getId())
          .thenReturn(documentId);

    when(pdfTextExtractor.extract(filePath))
          .thenReturn("");

    when(textChunkingService.chunk(""))
          .thenReturn(List.of());

    when(documentChunkRepository.saveAll(anyList()))
          .thenReturn(List.of());

    List<DocumentChunk> result =
          documentProcessingService.process(
                document,
                filePath
          );

    verify(documentChunkRepository)
          .deleteByDocumentId(documentId);

    verify(documentChunkRepository)
          .saveAll(List.of());

    assertEquals(0, result.size());
  }

  @Test
  void process_shouldRejectNullDocument() {
    Path filePath =
          Path.of("uploads/test.pdf");

    assertThrows(
          IllegalArgumentException.class,
          () -> documentProcessingService.process(
                null,
                filePath
          )
    );

    verifyNoInteractions(
          pdfTextExtractor,
          textChunkingService,
          documentChunkRepository
    );
  }

  @Test
  void process_shouldRejectNullFilePath() {
    Document document =
          mock(Document.class);

    assertThrows(
          IllegalArgumentException.class,
          () -> documentProcessingService.process(
                document,
                null
          )
    );

    verifyNoInteractions(
          pdfTextExtractor,
          textChunkingService,
          documentChunkRepository
    );
  }
}