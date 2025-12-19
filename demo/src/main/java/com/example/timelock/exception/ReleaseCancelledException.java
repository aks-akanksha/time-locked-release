package com.example.timelock.exception;

public class ReleaseCancelledException extends RuntimeException {
  public ReleaseCancelledException(Long id) {
    super("Release " + id + " has been cancelled");
  }
}

