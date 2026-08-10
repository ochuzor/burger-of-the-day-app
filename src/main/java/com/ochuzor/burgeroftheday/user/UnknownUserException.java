package com.ochuzor.burgeroftheday.user;

public class UnknownUserException extends RuntimeException {
  public UnknownUserException(String message) {
    super(message);
  }
}
