package com.djimbinov.platform.ai.model;

import java.util.List;

public record ChatRequestBody(
      String model,
      List<ChatMessage> messages
) {
}