package com.github.ramezch.backend.subscription.models;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.With;
import org.springframework.data.annotation.Id;

import java.time.Instant;

public record Subscription(
        @Id String id,
        @NotNull String customerId,
        @NotNull String internetPlanId,
        @With @FutureOrPresent Instant endDate,
        @With @NotNull SubscriptionStatus status
) {
    private static final int SEVEN_DAYS_GRACE_PERIOD_IN_SECONDS = 7 * 24 * 60 * 60;

    public boolean isExpiringSoon() {
        return status == SubscriptionStatus.ACTIVE
                && Instant.now().isAfter(endDate.minusSeconds(SEVEN_DAYS_GRACE_PERIOD_IN_SECONDS))
                && Instant.now().isBefore(endDate);
    }


    public boolean isGracePeriodOver() {
        Instant endDatePlusGracePeriod = endDate.plusSeconds(SEVEN_DAYS_GRACE_PERIOD_IN_SECONDS);
        return Instant.now().isAfter(endDatePlusGracePeriod);
    }
}