package com.github.ramezch.backend.exceptions;

public class CustomerSubscriptionNotFoundException extends RuntimeException {
    public CustomerSubscriptionNotFoundException(String message) {
        super("The Subscription for the customer with ID: " + message + " could not be found.");
    }
}
