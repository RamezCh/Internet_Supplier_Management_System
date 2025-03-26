package com.github.ramezch.backend.exceptions;

public class CustomerNotFoundException extends RuntimeException {
  public CustomerNotFoundException(String message) {
    super("The Customer with username: '" + message + "' could not be found.");
  }
}