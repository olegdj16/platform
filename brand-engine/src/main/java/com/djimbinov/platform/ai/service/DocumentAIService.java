package com.djimbinov.platform.ai.service;

import com.djimbinov.platform.ai.provider.AIProvider;
import com.djimbinov.platform.document.service.DocumentService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DocumentAIService {

  private final DocumentService documentService;
  private final AIProvider aiProvider;

  public DocumentAIService(
        DocumentService documentService,
        AIProvider aiProvider
  ) {
    this.documentService = documentService;
    this.aiProvider = aiProvider;
  }

  public String ask(
        UUID documentId,
        String question
  ) {

    String documentText =
          documentService.extractText(documentId);

    String prompt = """
          Answer the question using only the document provided below.

          If the answer cannot be found in the document, say:
          "The document does not contain that information."

          DOCUMENT:
          %s

          QUESTION:
          %s
          """.formatted(
          documentText,
          question
    );

    return aiProvider.chat(prompt);
  }
}