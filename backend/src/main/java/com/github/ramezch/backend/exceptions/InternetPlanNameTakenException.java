package com.github.ramezch.backend.exceptions;

public class InternetPlanNameTakenException extends RuntimeException {
    public InternetPlanNameTakenException(String message) {
        super(message);
    }
}
