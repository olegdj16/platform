package com.djimbinov.platform.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentQuestionRequest(

      @NotBlank(message = "Question is required")
      String question

) {
}