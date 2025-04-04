package com.github.ramezch.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@RestControllerAdvice
public class GlobalExceptionHandler {

    Logger logger = Logger.getLogger(getClass().getName());

    // Handle Failure at Data Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleIncorrectArgumentsException(MethodArgumentNotValidException exception) {
        logger.info("Customer not found: " + exception.getMessage());
        return new ErrorMessage(exception.getMessage(), LocalDateTime.now());
    }

    // Handle CustomerNotFoundException
    @ExceptionHandler(CustomerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleCustomerNotFoundException(CustomerNotFoundException exception) {
        logger.info("Customer not found: " + exception.getMessage());
        return new ErrorMessage(exception.getMessage(), LocalDateTime.now());
    }

    // Handle InternetPlanNotFoundException
    @ExceptionHandler(InternetPlanNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleInternetPlanNotFoundException(InternetPlanNotFoundException exception) {
        logger.info("Internet Plan not found: " + exception.getMessage());
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
}