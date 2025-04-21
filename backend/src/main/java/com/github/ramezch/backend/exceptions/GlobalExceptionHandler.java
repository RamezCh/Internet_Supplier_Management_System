package com.github.ramezch.backend.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.github.ramezch.backend.subscription.models.SubscriptionStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@RestControllerAdvice
public class GlobalExceptionHandler {

    Logger logger = Logger.getLogger(getClass().getName());

    // Handle Failure at Data Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<String> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .toList();

        if (!errors.isEmpty()) {
            String logMessage = String.format("Validation errors: %s at %s",
                    errors,
                    Instant.now());
            logger.info(logMessage);
        }

        return errors;
    }

    // Handle CustomerNotFoundException
    @ExceptionHandler(CustomerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleCustomerNotFoundException(CustomerNotFoundException exception) {
        logger.info("Customer not found: " + exception.getMessage());
        return new ErrorMessage(exception.getMessage(), LocalDateTime.now());
    }

    // Handle InvoiceNotFoundException
    @ExceptionHandler(InvoiceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleInvoiceNotFoundException(InvoiceNotFoundException exception) {
        logger.info("Invoice not found: " + exception.getMessage());
        return new ErrorMessage(exception.getMessage(), LocalDateTime.now());
    }

    // Handle InternetPlanNotFoundException
    @ExceptionHandler(InternetPlanNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleInternetPlanNotFoundException(InternetPlanNotFoundException exception) {
        logger.info("Internet Plan not found: " + exception.getMessage());
        return new ErrorMessage(exception.getMessage(), LocalDateTime.now());
    }

    // Handle CustomerSubscriptionNotFoundException
    @ExceptionHandler(CustomerSubscriptionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleCustomerSubscriptionNotFoundException(CustomerSubscriptionNotFoundException exception) {
        logger.info("The Subscription for the customer with ID: " + exception.getMessage() + " could not be found.");
        return new ErrorMessage(exception.getMessage(), LocalDateTime.now());
    }

    // Handle UsernameExistsException
    @ExceptionHandler(UsernameTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleUsernameExistsException(UsernameTakenException exception) {
        logger.info("Username already exists: " + exception.getMessage());
        return new ErrorMessage(exception.getMessage(), LocalDateTime.now());
    }

    // Handle InternetPlanNameTakenException
    @ExceptionHandler(InternetPlanNameTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleInternetPlanNameTakenException(InternetPlanNameTakenException exception) {
        logger.info("Internet Plan name already exists: " + exception.getMessage());
        return new ErrorMessage(exception.getMessage(), LocalDateTime.now());
    }

    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleAllExceptions(Exception exception) {
        // exception.printStackTrace(); shows everything that happened
        logger.severe("Unhandled exception occurred: " + exception.getMessage());
        return new ErrorMessage("An unexpected error occurred. Please try again later.", LocalDateTime.now());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String errorMessage = "Invalid request data";

        if (ex.getCause() instanceof InvalidFormatException ife && ife.getTargetType() != null && ife.getTargetType().isEnum()) {
            errorMessage = "Invalid status value. Allowed values: " + Arrays.toString(SubscriptionStatus.values());
        }
        return new ErrorMessage(errorMessage, LocalDateTime.now());
    }
}