package com.github.ramezch.backend.subscription.services;

import com.github.ramezch.backend.exceptions.CustomerSubscriptionNotFoundException;
import com.github.ramezch.backend.exceptions.InternetPlanNotFoundException;
import com.github.ramezch.backend.internetplan.models.InternetPlan;
import com.github.ramezch.backend.internetplan.repositories.InternetPlanRepository;
import com.github.ramezch.backend.subscription.models.Subscription;
import com.github.ramezch.backend.subscription.models.SubscriptionStatus;
import com.github.ramezch.backend.subscription.repository.SubscriptionRepository;
import com.github.ramezch.backend.utils.IdService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepo;
    @Mock
    private InternetPlanRepository internetPlanRepo;
    @Mock
    private IdService idService;
    @InjectMocks
    private SubscriptionService subscriptionService;

    private final String customerId = "customer-123";
    private final String internetPlanId = "plan-456";
    private final String subscriptionId = "sub-789";
    private final InternetPlan basicPlan = new InternetPlan(internetPlanId, "Basic Plan", "100Mbps", 29.99, "unlimited", true);

    @Test
    void createSubscription_shouldCreateNewSubscription_whenPlanExists() {
        // GIVEN
        Instant testStartTime = Instant.now();
        Subscription expectedSubscription = new Subscription(
                subscriptionId,
                customerId,
                internetPlanId,
                testStartTime,
                testStartTime.plus(30, ChronoUnit.DAYS),
                SubscriptionStatus.ACTIVE
        );

        when(internetPlanRepo.findById(internetPlanId)).thenReturn(Optional.of(basicPlan));
        when(idService.randomId()).thenReturn(subscriptionId);
        when(subscriptionRepo.save(any(Subscription.class))).thenReturn(expectedSubscription);

        // WHEN
        subscriptionService.createSubscription(customerId, internetPlanId);

        // THEN
        verify(internetPlanRepo).findById(internetPlanId);
        verify(idService).randomId();
        verify(subscriptionRepo).save(argThat(subscription ->
                subscription.id().equals(subscriptionId) &&
                        subscription.customerId().equals(customerId) &&
                        subscription.internetPlanId().equals(internetPlanId) &&
                        subscription.status() == SubscriptionStatus.ACTIVE &&
                        subscription.endDate().equals(subscription.startDate().plus(30, ChronoUnit.DAYS))
        ));
    }

    @Test
    void createSubscription_shouldThrowException_whenPlanNotFound() {
        // GIVEN
        when(internetPlanRepo.findById(internetPlanId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(InternetPlanNotFoundException.class,
                () -> subscriptionService.createSubscription(customerId, internetPlanId));

        verify(internetPlanRepo).findById(internetPlanId);
        verifyNoInteractions(idService, subscriptionRepo);
    }

    @Test
    void deleteSubscription_shouldDeleteExistingSubscription() {
        // GIVEN
        Subscription existingSubscription = new Subscription(
                subscriptionId,
                customerId,
                internetPlanId,
                Instant.now(),
                Instant.now().plus(30, ChronoUnit.DAYS),
                SubscriptionStatus.ACTIVE
        );

        when(subscriptionRepo.findByCustomerId(customerId)).thenReturn(Optional.of(existingSubscription));

        // WHEN
        subscriptionService.deleteSubscription(customerId);

        // THEN
        verify(subscriptionRepo).findByCustomerId(customerId);
        verify(subscriptionRepo).delete(existingSubscription);
    }

    @Test
    void deleteSubscription_shouldThrowException_whenSubscriptionNotFound() {
        // GIVEN
        when(subscriptionRepo.findByCustomerId(customerId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(CustomerSubscriptionNotFoundException.class,
                () -> subscriptionService.deleteSubscription(customerId));

        verify(subscriptionRepo).findByCustomerId(customerId);
        verifyNoMoreInteractions(subscriptionRepo);
    }

    @Test
    void createSubscription_shouldSetCorrectDuration() {
        // GIVEN
        Instant testStartTime = Instant.now();
        Subscription expectedSubscription = new Subscription(
                subscriptionId,
                customerId,
                internetPlanId,
                testStartTime,
                testStartTime.plus(30, ChronoUnit.DAYS),
                SubscriptionStatus.ACTIVE
        );

        when(internetPlanRepo.findById(internetPlanId)).thenReturn(Optional.of(basicPlan));
        when(idService.randomId()).thenReturn(subscriptionId);
        when(subscriptionRepo.save(any(Subscription.class))).thenReturn(expectedSubscription);

        // WHEN
        subscriptionService.createSubscription(customerId, internetPlanId);

        // THEN
        verify(subscriptionRepo).save(argThat(subscription ->
                subscription.endDate().equals(subscription.startDate().plus(30, ChronoUnit.DAYS))
        ));
    }

    @Test
    void createSubscription_shouldNotAllowNullPlanId() {
        // WHEN & THEN
        assertThrows(InternetPlanNotFoundException.class,
                () -> subscriptionService.createSubscription(customerId, null));
    }
}