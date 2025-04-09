package com.github.ramezch.backend.subscription.repository;

import com.github.ramezch.backend.subscription.models.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
}
