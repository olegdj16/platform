package com.djimbinov.platform.ai.controller;

import com.djimbinov.platform.ai.dto.ChatResponse;
import com.djimbinov.platform.ai.dto.DocumentQuestionRequest;
import com.djimbinov.platform.ai.service.DocumentAIService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/documents")
public class DocumentAIController {

  private final DocumentAIService documentAIService;

  public DocumentAIController(
        DocumentAIService documentAIService
  ) {
    this.documentAIService = documentAIService;
  }

  @PostMapping("/{documentId}/ask")
  public ChatResponse ask(
        @PathVariable UUID documentId,
        @Valid @RequestBody DocumentQuestionRequest request
  ) {

    String response = documentAIService.ask(
          documentId,
          request.question()
    );

    return new ChatResponse(response);
  }
}