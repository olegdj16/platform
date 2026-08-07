package com.djimbinov.platform.ai.service;

import com.djimbinov.platform.ai.provider.AIProvider;
import org.springframework.stereotype.Service;

@Service
public class AIChatService {

  private final AIProvider aiProvider;

  public AIChatService(AIProvider aiProvider) {
    this.aiProvider = aiProvider;
  }

  public String chat(String message) {
    return aiProvider.chat(message);
  }
}