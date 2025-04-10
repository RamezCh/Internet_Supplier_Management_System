package com.github.ramezch.backend.subscription.repository;

import com.github.ramezch.backend.subscription.models.Subscription;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    Optional<Subscription> findByCustomerId(@NotNull String customerId);
}
