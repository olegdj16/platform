package com.djimbinov.platform.exception;

import com.djimbinov.platform.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
        Exception exception
  ) {
    ApiResponse<Void> response =
          ApiResponse.error("An unexpected error occurred");

    return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(response);
  }
}