package com.github.ramezch.backend.subscription.services;

import com.github.ramezch.backend.exceptions.CustomerSubscriptionNotFoundException;
import com.github.ramezch.backend.exceptions.InternetPlanNotFoundException;
import com.github.ramezch.backend.internetplan.repositories.InternetPlanRepository;
import com.github.ramezch.backend.subscription.models.Subscription;
import com.github.ramezch.backend.subscription.models.SubscriptionDTO;
import com.github.ramezch.backend.subscription.models.SubscriptionStatus;
import com.github.ramezch.backend.subscription.repository.SubscriptionRepository;
import com.github.ramezch.backend.utils.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepo;
    private final InternetPlanRepository internetPlanRepo;
    private final IdService idService;

    public void createSubscription(String customerId, String internetPlanId) {
        internetPlanRepo.findById(internetPlanId)
                .orElseThrow(() -> new InternetPlanNotFoundException(internetPlanId));

        Instant startDate = Instant.now();
        Instant endDate = startDate.plus(30, ChronoUnit.DAYS); // Default 1 month duration

        Subscription subscription = new Subscription(
                idService.randomId(),
                customerId,
                internetPlanId,
                startDate,
                endDate,
                SubscriptionStatus.ACTIVE
        );

        subscriptionRepo.save(subscription);
    }

    public Optional<Subscription> getSubscription(String customerId) {
        return subscriptionRepo.findByCustomerId(customerId);
    }

    public Subscription updateSubscription(String customerId, SubscriptionDTO dto) {
        Subscription existing = subscriptionRepo.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomerSubscriptionNotFoundException(customerId));

        boolean unchanged = existing.customerId().equals(dto.customerId())
                && existing.internetPlanId().equals(dto.internetPlanId())
                && existing.endDate().equals(dto.endDate())
                && existing.status() == dto.status();

        if (unchanged) {
            return existing;
        }

        Subscription updated = new Subscription(
                existing.id(),
                dto.customerId(),
                dto.internetPlanId(),
                dto.startDate(),
                dto.endDate(),
                dto.status()
        );

        return subscriptionRepo.save(updated);
    }

    public void deleteSubscription(String customerId) {
        Subscription toDelete = subscriptionRepo.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomerSubscriptionNotFoundException(customerId));
        subscriptionRepo.delete(toDelete);
    }
}
