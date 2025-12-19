package com.example.timelock.exception;

public class ReleaseNotApprovedException extends RuntimeException {
  public ReleaseNotApprovedException(Long id) {
    super("Release " + id + " must be APPROVED before execution");
  }
}


