package com.github.ramezch.backend.subscription.controllers;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.exceptions.CustomerNotFoundException;
import com.github.ramezch.backend.exceptions.CustomerSubscriptionNotFoundException;
import com.github.ramezch.backend.subscription.models.Subscription;
import com.github.ramezch.backend.subscription.models.SubscriptionDTO;
import com.github.ramezch.backend.subscription.services.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @GetMapping("{customerId}")
    public Subscription getSubscription(@PathVariable String customerId, @AuthenticationPrincipal AppUser appUser) {
        if (!appUser.getCustomerIds().contains(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
        return subscriptionService.getSubscription(customerId)
                .orElseThrow(() -> new CustomerSubscriptionNotFoundException(customerId));
    }

    @PutMapping("{customerId}")
    public Subscription updateSubscription(@PathVariable String customerId, @Valid @RequestBody SubscriptionDTO dto, @AuthenticationPrincipal AppUser appUser) {
        if (!appUser.getCustomerIds().contains(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
        return subscriptionService.updateSubscription(customerId, dto);
    }

    @DeleteMapping("{customerId}")
    public void deleteSubscription(@PathVariable String customerId, @AuthenticationPrincipal AppUser appUser) {
        if (!appUser.getCustomerIds().contains(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
        subscriptionService.deleteSubscription(customerId);
    }
}
