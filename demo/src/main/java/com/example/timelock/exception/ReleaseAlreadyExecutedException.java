package com.example.timelock.exception;

public class ReleaseAlreadyExecutedException extends RuntimeException {
  public ReleaseAlreadyExecutedException(Long id) {
    super("Release " + id + " has already been executed");
  }
}


