package com.djimbinov.platform.document.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextChunkingServiceTest {

  private TextChunkingService textChunkingService;

  @BeforeEach
  void setUp() {
    textChunkingService = new TextChunkingService();
  }

  @Test
  void chunk_shouldReturnEmptyListWhenTextIsNull() {
    List<String> result =
          textChunkingService.chunk(null);

    assertTrue(result.isEmpty());
  }

  @Test
  void chunk_shouldReturnEmptyListWhenTextIsBlank() {
    List<String> result =
          textChunkingService.chunk("   ");

    assertTrue(result.isEmpty());
  }

  @Test
  void chunk_shouldReturnSingleChunkWhenTextIsSmall() {
    List<String> result =
          textChunkingService.chunk(
                "Brand Engine analyzes documents.",
                100,
                10
          );

    assertEquals(1, result.size());
    assertEquals(
          "Brand Engine analyzes documents.",
          result.getFirst()
    );
  }

  @Test
  void chunk_shouldCreateMultipleChunks() {
    String text =
          "This is the first sentence. "
                + "This is the second sentence. "
                + "This is the third sentence. "
                + "This is the fourth sentence.";

    List<String> result =
          textChunkingService.chunk(
                text,
                50,
                10
          );

    assertTrue(result.size() > 1);

    result.forEach(chunk ->
          assertFalse(chunk.isBlank())
    );
  }

  @Test
  void chunk_shouldNormalizeWhitespace() {
    String text =
          "Brand     Engine\r\n\r\n\r\n"
                + "processes     documents.";

    List<String> result =
          textChunkingService.chunk(
                text,
                200,
                20
          );

    assertEquals(1, result.size());
    assertEquals(
          "Brand Engine\n\nprocesses documents.",
          result.getFirst()
    );
  }

  @Test
  void chunk_shouldRejectInvalidChunkSize() {
    assertThrows(
          IllegalArgumentException.class,
          () -> textChunkingService.chunk(
                "text",
                0,
                0
          )
    );
  }

  @Test
  void chunk_shouldRejectNegativeOverlap() {
    assertThrows(
          IllegalArgumentException.class,
          () -> textChunkingService.chunk(
                "text",
                100,
                -1
          )
    );
  }

  @Test
  void chunk_shouldRejectOverlapEqualToChunkSize() {
    assertThrows(
          IllegalArgumentException.class,
          () -> textChunkingService.chunk(
                "text",
                100,
                100
          )
    );
  }
}