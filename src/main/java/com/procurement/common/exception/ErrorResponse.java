package com.procurement.common.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
  boolean success,
  String message,
  String path,
  Map<String, String> errors,
  LocalDateTime timestamp
) {
  public static ErrorResponse of(String message, String path, Map<String, String> errors) {
    return new ErrorResponse(false, message, path, errors, LocalDateTime.now());
  }
}
