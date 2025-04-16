package com.github.ramezch.backend.invoice.models;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.With;

import java.time.Instant;

public record Invoice(
        @NotBlank String id,
        @NotBlank String subscriptionId,
        @PastOrPresent Instant issueDate,
        @FutureOrPresent Instant dueDate,
        @PositiveOrZero double amountDue,
        @With @PositiveOrZero double amountPaid,
        @With @NotNull boolean isPaid
) {}