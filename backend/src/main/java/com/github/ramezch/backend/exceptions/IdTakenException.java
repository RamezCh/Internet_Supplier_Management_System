package com.github.ramezch.backend.exceptions;

public class IdTakenException extends RuntimeException {
  public IdTakenException(String message) {
    super("The Customer with id: '" + message + "' already exists.");
  }
}