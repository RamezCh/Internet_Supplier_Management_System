package com.github.ramezch.backend.exceptions;

public class UsernameTakenException extends RuntimeException {
  public UsernameTakenException(String message) {
    super("The Customer with username: '" + message + "' already exists.");
  }
}