package com.github.ramezch.backend.subscription.models;

import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.internetplan.models.InternetPlan;

import java.time.Instant;

public record SubscriptionDetailsDTO(
        String id,
        Customer customer,
        InternetPlan internetPlan,
        Instant endDate,
        SubscriptionStatus status
) {}