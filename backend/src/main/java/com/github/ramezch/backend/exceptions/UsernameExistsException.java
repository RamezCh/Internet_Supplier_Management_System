package com.github.ramezch.backend.exceptions;

public class UsernameExistsException extends RuntimeException {
    public UsernameExistsException(String message) {
        super("The Customer with username: " + message + " already exists.");
    }
}
