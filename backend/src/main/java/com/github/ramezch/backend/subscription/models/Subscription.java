package com.github.ramezch.backend.subscription.models;

import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.internetplan.models.InternetPlan;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.time.LocalDate;

public record Subscription(
        @Id String id,
        @NotNull Customer customer,
        @NotNull InternetPlan internetPlan,
        @NotNull Instant startDate,
        @FutureOrPresent LocalDate endDate,
        @NotNull SubscriptionStatus status,
        @Nullable String notes
) {
    public boolean isExpiringSoon() {
        return status == SubscriptionStatus.ACTIVE
                && LocalDate.now().isAfter(endDate.minusWeeks(1))
                && LocalDate.now().isBefore(endDate);
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }
}