package com.github.ramezch.backend.subscription.models;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;

import java.time.Instant;

public record Subscription(
        @Id String id,
        @NotNull String customerId,
        @NotNull String internetPlanId,
        @NotNull Instant startDate,
        @FutureOrPresent Instant endDate,
        @NotNull SubscriptionStatus status
) {
    public boolean isExpiringSoon() {
        return status == SubscriptionStatus.ACTIVE
                && Instant.now().isAfter(endDate.minusSeconds(604800)) // 7 days, 24hr in day, 60 minutes in an hour, 60 seconds in a minute
                && Instant.now().isBefore(endDate);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(endDate);
    }
}