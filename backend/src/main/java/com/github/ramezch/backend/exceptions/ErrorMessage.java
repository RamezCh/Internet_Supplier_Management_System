package com.github.ramezch.backend.exceptions;

import java.time.LocalDateTime;

public record ErrorMessage(String message, LocalDateTime timestamp) {
}