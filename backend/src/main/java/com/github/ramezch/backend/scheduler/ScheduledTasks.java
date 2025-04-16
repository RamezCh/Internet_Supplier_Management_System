package com.github.ramezch.backend.scheduler;


import com.github.ramezch.backend.internetplan.models.InternetPlan;
import com.github.ramezch.backend.internetplan.repositories.InternetPlanRepository;
import com.github.ramezch.backend.invoice.models.Invoice;
import com.github.ramezch.backend.invoice.models.InvoiceDTO;
import com.github.ramezch.backend.invoice.services.InvoiceService;
import com.github.ramezch.backend.subscription.models.Subscription;
import com.github.ramezch.backend.subscription.models.SubscriptionStatus;
import com.github.ramezch.backend.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduledTasks {

    private final SubscriptionRepository subscriptionRepository;
    private final InternetPlanRepository internetPlanRepository;
    private final InvoiceService invoiceService;
    private static final String DAILY_AT_MIDNIGHT = "0 0 * * *";

    @Scheduled(cron = DAILY_AT_MIDNIGHT)
    public void updateSubscriptionStatus() {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        for(Subscription subscription : subscriptions) {
            if(subscription.isExpiringSoon()) {
                subscriptionRepository.save(subscription.withStatus(SubscriptionStatus.EXPIRING));
            }
            Invoice subscriptionInvoice = invoiceService.getInvoice(subscription.id(), subscription.endDate());

            if (subscription.isGracePeriodOver() && subscriptionInvoice.isPaid()) {
                Instant newEndDate = subscription.endDate().plus(Duration.ofDays(30));
                subscriptionRepository.save(
                        subscription.withStatus(SubscriptionStatus.ACTIVE)
                                .withEndDate(newEndDate)
                );

                InternetPlan subInternetPlan = internetPlanRepository.findByIdInAndIsActive(List.of(subscription.internetPlanId()), true).getFirst();
                double invoiceAmountDue = subInternetPlan.price();
                InvoiceDTO newInvoiceDTO = new InvoiceDTO(subscription.id(), newEndDate, invoiceAmountDue);
                invoiceService.generateInvoice(newInvoiceDTO);
            }

            if(subscription.isGracePeriodOver() && !subscriptionInvoice.isPaid()) {
                subscriptionRepository.save(subscription.withStatus(SubscriptionStatus.EXPIRED));
            }
        }
    }
}