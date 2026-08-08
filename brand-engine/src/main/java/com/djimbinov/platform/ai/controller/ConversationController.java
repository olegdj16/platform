package com.djimbinov.platform.ai.controller;

import com.djimbinov.platform.ai.dto.ChatRequest;
import com.djimbinov.platform.ai.dto.ChatResponse;
import com.djimbinov.platform.ai.dto.ConversationRequest;
import com.djimbinov.platform.ai.dto.ConversationResponse;
import com.djimbinov.platform.ai.service.ConversationAIService;
import com.djimbinov.platform.ai.service.ConversationService;
import com.djimbinov.platform.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/conversations")
public class ConversationController {

  private final ConversationService conversationService;
  private final ConversationAIService conversationAIService;

  public ConversationController(
        ConversationService conversationService,
        ConversationAIService conversationAIService
  ) {
    this.conversationService = conversationService;
    this.conversationAIService = conversationAIService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ConversationResponse>> create(
        @Valid @RequestBody ConversationRequest request
  ) {

    ConversationResponse conversation =
          conversationService.createConversation(request);

    return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(ApiResponse.success(
                "Conversation created successfully",
                conversation
          ));
  }

  @GetMapping("/{id}")
  public ApiResponse<ConversationResponse> findById(
        @PathVariable UUID id
  ) {

    return ApiResponse.success(
          "Conversation loaded successfully",
          conversationService.getConversation(id)
    );
  }

  @GetMapping
  public ApiResponse<List<ConversationResponse>> findAll() {

    return ApiResponse.success(
          "Conversations loaded successfully",
          conversationService.getConversations()
    );
  }

  @PostMapping("/{conversationId}/chat")
  public ChatResponse chat(
        @PathVariable UUID conversationId,
        @Valid @RequestBody ChatRequest request
  ) {

    String response =
          conversationAIService.chat(
                conversationId,
                request.message()
          );

    return new ChatResponse(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
        @PathVariable UUID id
  ) {

    conversationService.deleteConversation(id);

    return ResponseEntity.noContent().build();
  }
}