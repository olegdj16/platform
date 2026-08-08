package com.djimbinov.platform.ai.rag;

import com.djimbinov.platform.document.model.DocumentChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RagPromptBuilderTest {

  private RagPromptBuilder ragPromptBuilder;

  @BeforeEach
  void setUp() {
    ragPromptBuilder =
          new RagPromptBuilder();
  }

  @Test
  void build_shouldIncludeQuestionAndChunkContent() {
    DocumentChunk firstChunk =
          mock(DocumentChunk.class);

    DocumentChunk secondChunk =
          mock(DocumentChunk.class);

    when(firstChunk.getContent())
          .thenReturn(
                "Authentication uses JWT tokens."
          );

    when(secondChunk.getContent())
          .thenReturn(
                "Tokens expire after 24 hours."
          );

    String result =
          ragPromptBuilder.build(
                "How does authentication work?",
                List.of(
                      firstChunk,
                      secondChunk
                )
          );

    assertTrue(
          result.contains(
                "How does authentication work?"
          )
    );

    assertTrue(
          result.contains(
                "Authentication uses JWT tokens."
          )
    );

    assertTrue(
          result.contains(
                "Tokens expire after 24 hours."
          )
    );

    assertTrue(
          result.contains("[Source 1]")
    );

    assertTrue(
          result.contains("[Source 2]")
    );
  }

  @Test
  void build_shouldWorkWithEmptyChunkList() {
    String result =
          ragPromptBuilder.build(
                "What is the target market?",
                List.of()
          );

    assertTrue(
          result.contains(
                "What is the target market?"
          )
    );

    assertTrue(
          result.contains(
                "DOCUMENT CONTEXT:"
          )
    );
  }

  @Test
  void build_shouldRejectNullQuestion() {
    assertThrows(
          IllegalArgumentException.class,
          () -> ragPromptBuilder.build(
                null,
                List.of()
          )
    );
  }

  @Test
  void build_shouldRejectBlankQuestion() {
    assertThrows(
          IllegalArgumentException.class,
          () -> ragPromptBuilder.build(
                "   ",
                List.of()
          )
    );
  }

  @Test
  void build_shouldRejectNullChunks() {
    assertThrows(
          IllegalArgumentException.class,
          () -> ragPromptBuilder.build(
                "Question",
                null
          )
    );
  }
}