package com.djimbinov.platform.exception;

import com.djimbinov.platform.common.ApiResponse;
import com.djimbinov.platform.document.exception.DocumentNotFoundException;
import com.djimbinov.platform.organization.exception.OrganizationNotFoundException;
import com.djimbinov.platform.project.exception.ProjectAlreadyExistsException;
import com.djimbinov.platform.project.exception.ProjectNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
        BadCredentialsException exception
  ) {
    ApiResponse<Void> response = new ApiResponse<>(
          false,
          "Invalid email or password",
          null
    );

    return ResponseEntity
          .status(HttpStatus.UNAUTHORIZED)
          .body(response);
  }

  @ExceptionHandler(OrganizationNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleOrganizationNotFound(
        OrganizationNotFoundException exception
  ) {
    return ResponseEntity
          .status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error(exception.getMessage()));
  }

  @ExceptionHandler(ProjectAlreadyExistsException.class)
  public ResponseEntity<ApiResponse<Void>> handleProjectAlreadyExists(
        ProjectAlreadyExistsException exception
  ) {
    return ResponseEntity
          .status(HttpStatus.CONFLICT)
          .body(ApiResponse.error(exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(
        MethodArgumentNotValidException exception
  ) {
    String message = exception.getBindingResult()
          .getFieldErrors()
          .stream()
          .map(error -> error.getField() + ": "
                + error.getDefaultMessage())
          .collect(Collectors.joining(", "));

    return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(message));
  }

  // Database safeguard in case two identical requests arrive simultaneously.
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
        DataIntegrityViolationException exception
  ) {
    return ResponseEntity
          .status(HttpStatus.CONFLICT)
          .body(ApiResponse.error(
                "The requested record conflicts with existing data."
          ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
        Exception exception
  ) {
    return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(
                "An unexpected error occurred."
          ));
  }

  @ExceptionHandler(ProjectNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleProjectNotFound(
        ProjectNotFoundException ex
  ) {
    return ResponseEntity
          .status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error(ex.getMessage()));
  }

  @ExceptionHandler(DocumentNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleDocumentNotFound(
        DocumentNotFoundException exception
  ) {
    return ResponseEntity
          .status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error(exception.getMessage()));
  }
}