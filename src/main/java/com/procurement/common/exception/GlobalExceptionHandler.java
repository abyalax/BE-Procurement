package com.procurement.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ErrorResponse> handleApiException(
    ApiException exception,
    HttpServletRequest request
  ) {
    return build(exception.getStatus(), exception.getMessage(), request, Map.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> handleValidation(
    MethodArgumentNotValidException exception,
    HttpServletRequest request
  ) {
    Map<String, String> errors = new LinkedHashMap<>();
    for (FieldError error : exception.getBindingResult().getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }
    return build(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);
  }

  @ExceptionHandler({ BadCredentialsException.class, AuthenticationException.class })
  ResponseEntity<ErrorResponse> handleAuthentication(
    Exception exception,
    HttpServletRequest request
  ) {
    return build(HttpStatus.UNAUTHORIZED, "Invalid credentials", request, Map.of());
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ErrorResponse> handleAccessDenied(
    AccessDeniedException exception,
    HttpServletRequest request
  ) {
    return build(HttpStatus.FORBIDDEN, "Access denied", request, Map.of());
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
    log.error("Unhandled exception path={}", request.getRequestURI(), exception);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request, Map.of());
  }

  private ResponseEntity<ErrorResponse> build(
    HttpStatus status,
    String message,
    HttpServletRequest request,
    Map<String, String> errors
  ) {
    return ResponseEntity.status(status).body(
      ErrorResponse.of(message, request.getRequestURI(), errors)
    );
  }
}
