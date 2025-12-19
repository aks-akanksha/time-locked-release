package com.example.timelock.api;

import com.example.timelock.exception.*;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {
  private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

  private ResponseEntity<Map<String,Object>> body(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(Map.of(
      "timestamp", Instant.now().toString(),
      "status", status.value(),
      "error", status.getReasonPhrase(),
      "message", message
    ));
  }

  @ExceptionHandler(ReleaseNotFoundException.class)
  public ResponseEntity<?> handleNotFound(ReleaseNotFoundException ex) {
    log.warn("Release not found: {}", ex.getMessage());
    return body(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler({ReleaseNotApprovedException.class, ReleaseNotScheduledException.class, 
                     ReleaseExecutionTooEarlyException.class, ReleaseAlreadyExecutedException.class,
                     ReleaseCancelledException.class})
  public ResponseEntity<?> handleBusinessRuleViolations(RuntimeException ex) {
    log.warn("Business rule violation: {}", ex.getMessage());
    return body(HttpStatus.CONFLICT, ex.getMessage()); // 409 for business rules
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<?> illegalState(IllegalStateException ex) {
    log.warn("Illegal state: {}", ex.getMessage());
    return body(HttpStatus.CONFLICT, ex.getMessage()); // 409 for business rules
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<?> badPathVars(MethodArgumentTypeMismatchException ex) {
    log.warn("Invalid path variable: {}", ex.getMessage());
    return body(HttpStatus.BAD_REQUEST, "Invalid request parameter.");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> badArgs(IllegalArgumentException ex) {
    log.warn("Invalid argument: {}", ex.getMessage());
    return body(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
    String errors = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));
    log.warn("Validation error: {}", errors);
    return body(HttpStatus.BAD_REQUEST, "Validation failed: " + errors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<?> handleConstraintViolations(ConstraintViolationException ex) {
    String errors = ex.getConstraintViolations().stream()
        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
        .collect(Collectors.joining(", "));
    log.warn("Constraint violation: {}", errors);
    return body(HttpStatus.BAD_REQUEST, "Validation failed: " + errors);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGeneric(Exception ex) {
    log.error("Unexpected error", ex);
    return body(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
  }
}
