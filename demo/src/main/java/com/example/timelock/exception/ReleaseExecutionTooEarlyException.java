package com.example.timelock.exception;

import java.time.Instant;

public class ReleaseExecutionTooEarlyException extends RuntimeException {
  public ReleaseExecutionTooEarlyException(Long id, Instant scheduledAt) {
    super("Cannot execute release " + id + " before scheduled time: " + scheduledAt);
  }
}

