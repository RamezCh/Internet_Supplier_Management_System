package com.github.ramezch.backend.subscription.services;

import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import com.github.ramezch.backend.exceptions.CustomerNotFoundException;
import com.github.ramezch.backend.exceptions.CustomerSubscriptionNotFoundException;
import com.github.ramezch.backend.exceptions.InternetPlanNotFoundException;
import com.github.ramezch.backend.internetplan.models.InternetPlan;
import com.github.ramezch.backend.internetplan.repositories.InternetPlanRepository;
import com.github.ramezch.backend.invoice.models.InvoiceDTO;
import com.github.ramezch.backend.invoice.services.InvoiceService;
import com.github.ramezch.backend.subscription.models.Subscription;
import com.github.ramezch.backend.subscription.models.SubscriptionDTO;
import com.github.ramezch.backend.subscription.models.SubscriptionDetailsDTO;
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
    private final CustomerRepository customerRepo;
    private final IdService idService;
    private final InvoiceService invoiceService;

    public void createSubscription(String customerId, String internetPlanId) {
       InternetPlan internetPlan =  internetPlanRepo.findById(internetPlanId)
                .orElseThrow(() -> new InternetPlanNotFoundException(internetPlanId));

        Instant startDate = Instant.now();
        Instant endDate = startDate.plus(30, ChronoUnit.DAYS); // Default 1 month duration

        String subId = idService.randomId();

        Subscription subscription = new Subscription(
                subId,
                customerId,
                internetPlanId,
                endDate,
                SubscriptionStatus.ACTIVE
        );

        InvoiceDTO newInvoiceDTO = new InvoiceDTO(customerId, subId, endDate, internetPlan.price());
        invoiceService.generateInvoice(newInvoiceDTO);

        subscriptionRepo.save(subscription);
    }

    public Optional<SubscriptionDetailsDTO> getSubscription(String customerId) {
        return subscriptionRepo.findByCustomerId(customerId)
                .map(subscription -> {
                    InternetPlan internetPlan = internetPlanRepo.findById(subscription.internetPlanId())
                            .orElseThrow(() -> new InternetPlanNotFoundException(subscription.internetPlanId()));

                    Customer customer = customerRepo.findById(subscription.customerId())
                            .orElseThrow(() -> new CustomerNotFoundException(subscription.customerId()));

                    return new SubscriptionDetailsDTO(
                            subscription.id(),
                            customer,
                            internetPlan,
                            subscription.endDate(),
                            subscription.status()
                    );
                });
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
