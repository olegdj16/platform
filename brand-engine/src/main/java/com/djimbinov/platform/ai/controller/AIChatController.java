package com.djimbinov.platform.ai.controller;

import com.djimbinov.platform.ai.dto.ChatRequest;
import com.djimbinov.platform.ai.dto.ChatResponse;
import com.djimbinov.platform.ai.service.AIChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AIChatController {

  private final AIChatService aiChatService;

  public AIChatController(AIChatService aiChatService) {
    this.aiChatService = aiChatService;
  }

  @PostMapping("/chat")
  public ChatResponse chat(@Valid @RequestBody ChatRequest request) {

    String response = aiChatService.chat(request.message());

    return new ChatResponse(response);
  }
}