package com.github.ramezch.backend.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super("AppUser not found with id: " + message + " already exists.");
    }
}
