package com.example.timelock.exception;

public class ReleaseNotScheduledException extends RuntimeException {
  public ReleaseNotScheduledException(Long id) {
    super("Release " + id + " must be scheduled before execution");
  }
}


