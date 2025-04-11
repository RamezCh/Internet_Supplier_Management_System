package com.github.ramezch.backend.subscription.models;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record SubscriptionDTO(
        @NotNull String customerId,
        @NotNull String internetPlanId,
        @NotNull Instant startDate,
        @FutureOrPresent Instant endDate,
        @NotNull SubscriptionStatus status
) {
}
