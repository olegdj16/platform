package com.djimbinov.platform.ai.rag;

import com.djimbinov.platform.document.model.DocumentChunk;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RagPromptBuilder {

  public String build(
        String question,
        List<DocumentChunk> chunks
  ) {
    if (question == null || question.isBlank()) {
      throw new IllegalArgumentException(
            "Question must not be null or blank"
      );
    }

    if (chunks == null) {
      throw new IllegalArgumentException(
            "Chunks must not be null"
      );
    }

    StringBuilder context =
          new StringBuilder();

    for (int index = 0; index < chunks.size(); index++) {
      DocumentChunk chunk = chunks.get(index);

      context
            .append("[Source ")
            .append(index + 1)
            .append("]\n")
            .append(chunk.getContent())
            .append("\n\n");
    }

    return """
          You are an AI assistant for Brand Engine.

          Answer the user's question using the provided document context.

          Rules:
          - Base the answer on the supplied context.
          - Do not invent information that is not supported by the context.
          - If the context does not contain enough information, say so.
          - Give a concise and direct answer.

          DOCUMENT CONTEXT:
          %s

          USER QUESTION:
          %s
          """.formatted(
          context.toString().trim(),
          question.trim()
    );
  }
}