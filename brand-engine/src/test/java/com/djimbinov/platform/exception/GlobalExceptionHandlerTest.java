package com.djimbinov.platform.exception;

import com.djimbinov.platform.ai.exception.AIServiceException;
import com.djimbinov.platform.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler =
        new GlobalExceptionHandler();

  @Test
  void handleAIServiceExceptionShouldReturnBadGateway() {

    AIServiceException exception =
          new AIServiceException("OpenAI failed");

    ResponseEntity<ApiResponse<Void>> response =
          handler.handleAIServiceException(exception);

    assertEquals(
          HttpStatus.BAD_GATEWAY,
          response.getStatusCode()
    );

    assertNotNull(response.getBody());

    assertEquals(
          false,
          response.getBody().isSuccess()
    );

    assertEquals(
          "AI service is temporarily unavailable.",
          response.getBody().getMessage()
    );
  }
}