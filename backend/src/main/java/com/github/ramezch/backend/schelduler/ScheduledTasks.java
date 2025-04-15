package com.github.ramezch.backend.schelduler;

import com.github.ramezch.backend.subscription.models.Subscription;
import com.github.ramezch.backend.subscription.models.SubscriptionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class ScheduledTasks {

    private final String DAILY_AT_MIDNIGHT_IN_CRON = "0 0 0 * * *";
    @Scheduled(cron = DAILY_AT_MIDNIGHT_IN_CRON)
    public void handleSubscriptionRenewals() {
        Instant now = Instant.now();
        Instant gracePeriodEnd = now.minus(7, ChronoUnit.DAYS);

        List<Subscription> activeSubscriptionsPastDue = subscriptionRepo
                .findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, gracePeriodEnd);

        activeSubscriptionsPastDue.forEach(sub -> {
            // Extend subscription by 1 month (or your billing cycle)
            Instant newEndDate = sub.endDate().plus(30, ChronoUnit.DAYS);
            Subscription renewedSub = sub.withEndDate(newEndDate);
            subscriptionRepo.save(renewedSub);

            // Generate a new invoice for the next period
            invoiceService.generateInvoiceForSubscription(renewedSub);
        });
    }

}
