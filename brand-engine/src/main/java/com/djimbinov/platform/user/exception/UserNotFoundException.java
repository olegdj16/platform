package com.djimbinov.platform.user.exception;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(String email) {
    super("User not found: " + email);
  }
}