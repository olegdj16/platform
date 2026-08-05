package com.djimbinov.platform.user.exception;

public class InvalidPasswordException extends RuntimeException {

  public InvalidPasswordException() {
    super("Current password is incorrect");
  }
}