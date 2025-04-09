package com.github.ramezch.backend.subscription.services;

import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.exceptions.InternetPlanNotFoundException;
import com.github.ramezch.backend.internetplan.models.InternetPlan;
import com.github.ramezch.backend.internetplan.repositories.InternetPlanRepository;
import com.github.ramezch.backend.subscription.models.Subscription;
import com.github.ramezch.backend.subscription.models.SubscriptionStatus;
import com.github.ramezch.backend.subscription.repository.SubscriptionRepository;
import com.github.ramezch.backend.utils.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepo;
    private final InternetPlanRepository internetPlanRepo;
    private final IdService idService;

    public void createSubscription(Customer customer, String internetPlanId) {
        InternetPlan plan = internetPlanRepo.findById(internetPlanId)
                .orElseThrow(() -> new InternetPlanNotFoundException(internetPlanId));

        Instant startDate = Instant.now();
        Instant endDate = startDate.plus(30, ChronoUnit.DAYS); // Default 1 month duration

        Subscription subscription = new Subscription(
                idService.randomId(),
                customer,
                plan,
                startDate,
                endDate,
                SubscriptionStatus.ACTIVE
        );

        subscriptionRepo.save(subscription);
    }
}
