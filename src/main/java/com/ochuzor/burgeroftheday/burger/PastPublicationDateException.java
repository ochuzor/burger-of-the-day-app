package com.ochuzor.burgeroftheday.burger;

public class PastPublicationDateException extends RuntimeException {
  public PastPublicationDateException(String message) {
    super(message);
  }
}
