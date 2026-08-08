package com.djimbinov.platform.ai.service;

import com.djimbinov.platform.ai.provider.AIProvider;
import com.djimbinov.platform.ai.rag.DocumentRetrievalService;
import com.djimbinov.platform.ai.rag.RagPromptBuilder;
import com.djimbinov.platform.document.model.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentAIService {

  private final DocumentRetrievalService documentRetrievalService;
  private final RagPromptBuilder ragPromptBuilder;
  private final AIProvider aiProvider;

  public DocumentAIService(
        DocumentRetrievalService documentRetrievalService,
        RagPromptBuilder ragPromptBuilder,
        AIProvider aiProvider
  ) {
    this.documentRetrievalService = documentRetrievalService;
    this.ragPromptBuilder = ragPromptBuilder;
    this.aiProvider = aiProvider;
  }

  public String ask(
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

    List<DocumentChunk> chunks =
          documentRetrievalService.retrieve(
                projectId,
                question
          );

    String prompt =
          ragPromptBuilder.build(
                question,
                chunks
          );

    return aiProvider.chat(prompt);
  }
}