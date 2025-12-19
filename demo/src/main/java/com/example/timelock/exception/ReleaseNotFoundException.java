package com.example.timelock.exception;

public class ReleaseNotFoundException extends RuntimeException {
  public ReleaseNotFoundException(Long id) {
    super("Release with id " + id + " not found");
  }
}


