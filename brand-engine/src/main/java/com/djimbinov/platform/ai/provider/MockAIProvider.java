package com.djimbinov.platform.ai.provider;

import org.springframework.stereotype.Component;

@Component
public class MockAIProvider implements AIProvider {

  @Override
  public String chat(String prompt) {
    return "AI response for: " + prompt;
  }
}