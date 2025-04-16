package com.github.ramezch.backend.invoice.models;

import jakarta.validation.constraints.*;
import lombok.With;

import java.time.Instant;

public record InvoiceDTO(
        @NotBlank String subscriptionId,
        @FutureOrPresent Instant dueDate,
        @With @PositiveOrZero double amountDue
) {}