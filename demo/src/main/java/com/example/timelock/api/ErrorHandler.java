package com.example.timelock.api;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

  private ResponseEntity<Map<String,Object>> body(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(Map.of(
      "timestamp", Instant.now().toString(),
      "status", status.value(),
      "error", status.getReasonPhrase(),
      "message", message
    ));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<?> illegalState(IllegalStateException ex) {
    return body(HttpStatus.CONFLICT, ex.getMessage()); // 409 for business rules
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<?> badPathVars(MethodArgumentTypeMismatchException ex) {
    return body(HttpStatus.BAD_REQUEST, "Invalid request parameter.");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> badArgs(IllegalArgumentException ex) {
    return body(HttpStatus.BAD_REQUEST, ex.getMessage());
  }
}
