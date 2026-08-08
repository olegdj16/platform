package com.djimbinov.platform.document.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkingService {

  private static final int DEFAULT_CHUNK_SIZE = 2000;
  private static final int DEFAULT_OVERLAP = 200;

  public List<String> chunk(String text) {
    return chunk(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
  }

  public List<String> chunk(
        String text,
        int chunkSize,
        int overlap
  ) {
    if (text == null || text.isBlank()) {
      return List.of();
    }

    if (chunkSize <= 0) {
      throw new IllegalArgumentException(
            "Chunk size must be greater than zero"
      );
    }

    if (overlap < 0 || overlap >= chunkSize) {
      throw new IllegalArgumentException(
            "Overlap must be greater than or equal to zero "
                  + "and smaller than chunk size"
      );
    }

    String normalized = normalize(text);

    List<String> chunks = new ArrayList<>();

    int start = 0;

    while (start < normalized.length()) {
      int end = Math.min(
            start + chunkSize,
            normalized.length()
      );

      if (end < normalized.length()) {
        end = findNaturalBoundary(
              normalized,
              start,
              end
        );
      }

      String chunk = normalized
            .substring(start, end)
            .trim();

      if (!chunk.isEmpty()) {
        chunks.add(chunk);
      }

      if (end >= normalized.length()) {
        break;
      }

      int nextStart = end - overlap;

      if (nextStart <= start) {
        nextStart = end;
      }

      start = nextStart;
    }

    return chunks;
  }

  private String normalize(String text) {
    return text
          .replace("\r\n", "\n")
          .replace('\r', '\n')
          .replaceAll("[ \\t]+", " ")
          .replaceAll("\\n{3,}", "\n\n")
          .trim();
  }

  private int findNaturalBoundary(
        String text,
        int start,
        int proposedEnd
  ) {
    int minimumBoundary =
          start + ((proposedEnd - start) / 2);

    int paragraph =
          text.lastIndexOf("\n\n", proposedEnd);

    if (paragraph >= minimumBoundary) {
      return paragraph + 2;
    }

    int sentence =
          text.lastIndexOf(". ", proposedEnd);

    if (sentence >= minimumBoundary) {
      return sentence + 1;
    }

    int whitespace =
          text.lastIndexOf(' ', proposedEnd);

    if (whitespace >= minimumBoundary) {
      return whitespace;
    }

    return proposedEnd;
  }
}