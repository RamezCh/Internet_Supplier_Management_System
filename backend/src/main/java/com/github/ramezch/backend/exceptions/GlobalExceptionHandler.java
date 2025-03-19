package com.github.ramezch.backend.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@RestControllerAdvice
public class GlobalExceptionHandler {

    Logger logger = Logger.getLogger(getClass().getName());

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleAllExceptions(Exception exception) {
        logger.info("Unhandled exception occurred: " + exception.getMessage());
        return new ErrorMessage("An unexpected error occurred. Please try again later.", LocalDateTime.now());
    }
}

