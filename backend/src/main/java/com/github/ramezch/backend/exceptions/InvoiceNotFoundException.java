package com.github.ramezch.backend.exceptions;

public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(String message) {
        super("Invoice with id: '" + message + "' not found.");
    }
}
