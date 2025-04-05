package com.github.ramezch.backend.exceptions;

public class InternetPlanNotFoundException extends RuntimeException {
  public InternetPlanNotFoundException(String message) {
    super("Internet plan with id: '" + message + "' not found.");
  }
}
