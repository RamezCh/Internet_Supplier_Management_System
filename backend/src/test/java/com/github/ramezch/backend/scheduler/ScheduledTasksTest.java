package com.github.ramezch.backend.scheduler;

import com.github.ramezch.backend.internetplan.models.InternetPlan;
import com.github.ramezch.backend.internetplan.repositories.InternetPlanRepository;
import com.github.ramezch.backend.invoice.models.Invoice;
import com.github.ramezch.backend.invoice.models.InvoiceDTO;
import com.github.ramezch.backend.invoice.services.InvoiceService;
import com.github.ramezch.backend.subscription.models.Subscription;
import com.github.ramezch.backend.subscription.models.SubscriptionStatus;
import com.github.ramezch.backend.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledTasksTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InternetPlanRepository internetPlanRepository;

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private ScheduledTasks scheduledTasks;

    private Instant currentTime;
    private Subscription activeSubscription;
    private Subscription expiringSubscription;
    private Subscription expiredSubscription;
    private Subscription cancelledSubscription;

    @BeforeEach
    void setUp() {
        currentTime = Instant.now();

        activeSubscription = new Subscription(
                "sub1", "cust1", "plan1",
                currentTime.plus(Duration.ofDays(10)),
                SubscriptionStatus.ACTIVE
        );

        expiringSubscription = new Subscription(
                "sub2", "cust2", "plan2",
                currentTime.plus(Duration.ofDays(6)),
                SubscriptionStatus.ACTIVE
        );

        expiredSubscription = new Subscription(
                "sub3", "cust3", "plan3",
                currentTime.minus(Duration.ofDays(10)),
                SubscriptionStatus.EXPIRING
        );

        cancelledSubscription = new Subscription(
                "sub4", "cust4", "plan4",
                currentTime.plus(Duration.ofDays(1)),
                SubscriptionStatus.CANCELLED
        );
    }

    @Test
    void updateSubscriptionStatus_ShouldProcessAllActiveSubscriptions() {
        // Given
        when(subscriptionRepository.findAll()).thenReturn(List.of(
                activeSubscription, expiringSubscription, expiredSubscription, cancelledSubscription
        ));

        Invoice paidInvoice = new Invoice(
                "inv1", "cust3", "sub3",
                currentTime.minus(Duration.ofDays(10)),
                currentTime.minus(Duration.ofDays(5)),
                100.0, 100.0, true
        );
        when(invoiceService.getInvoice(eq("sub3"), any())).thenReturn(paidInvoice);

        // When
        scheduledTasks.updateSubscriptionStatus();

        // Then
        verify(subscriptionRepository).findAll();
        verify(subscriptionRepository).save(argThat(sub ->
                sub.id().equals("sub2") &&
                        sub.status() == SubscriptionStatus.EXPIRING
        ));
        verify(subscriptionRepository).save(argThat(sub ->
                sub.id().equals("sub3") &&
                        sub.status() == SubscriptionStatus.ACTIVE &&
                        sub.endDate().isAfter(expiredSubscription.endDate())
        ));
        verify(subscriptionRepository, never()).save(argThat(sub ->
                sub.id().equals("sub4")
        ));
    }

    @Test
    void processSubscription_ShouldMarkAsExpiringWhenEndDateIsNear() {
        // Given
        Subscription subscription = expiringSubscription;

        // When
        scheduledTasks.processSubscription(subscription);

        // Then
        verify(subscriptionRepository).save(argThat(sub ->
                sub.id().equals("sub2") &&
                        sub.status() == SubscriptionStatus.EXPIRING
        ));
        verify(invoiceService, never()).getInvoice(any(), any());
    }

    @Test
    void processSubscription_ShouldRenewWhenPaidAndGracePeriodOver() {
        // Given
        Subscription subscription = expiredSubscription;
        Invoice paidInvoice = new Invoice(
                "inv1", "cust3", "sub3",
                currentTime.minus(Duration.ofDays(40)),
                currentTime.minus(Duration.ofDays(10)),
                100.0, 100.0, true
        );
        InternetPlan plan3 = new InternetPlan("plan3", "basic", "100Mbps", 72, "unlimited", true);
        when(invoiceService.getInvoice(subscription.id(), subscription.endDate())).thenReturn(paidInvoice);
        when(internetPlanRepository.findByIdInAndIsActive(List.of(subscription.internetPlanId()), true)).thenReturn(List.of(plan3));

        // When
        scheduledTasks.processSubscription(subscription);

        // Then
        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(subscriptionCaptor.capture());

        Subscription savedSubscription = subscriptionCaptor.getValue();
        assertEquals(SubscriptionStatus.ACTIVE, savedSubscription.status());
        assertTrue(savedSubscription.endDate().isAfter(subscription.endDate()));

        ArgumentCaptor<InvoiceDTO> invoiceCaptor = ArgumentCaptor.forClass(InvoiceDTO.class);
        verify(invoiceService).generateInvoice(invoiceCaptor.capture());

        InvoiceDTO newInvoice = invoiceCaptor.getValue();
        assertEquals("cust3", newInvoice.customerId());
        assertEquals("sub3", newInvoice.subscriptionId());
        assertEquals(savedSubscription.endDate(), newInvoice.dueDate());
    }

    @Test
    void processSubscription_ShouldMarkAsExpiredWhenUnpaidAndGracePeriodOver() {
        // Given
        Subscription subscription = expiredSubscription;
        Invoice unpaidInvoice = new Invoice(
                "inv1", "cust3", "sub3",
                currentTime.minus(Duration.ofDays(20)),
                currentTime.minus(Duration.ofDays(9)),
                100.0, 0.0, false
        );
        when(invoiceService.getInvoice(subscription.id(), subscription.endDate())).thenReturn(unpaidInvoice);

        // When
        scheduledTasks.processSubscription(subscription);

        // Then
        verify(subscriptionRepository).save(argThat(sub ->
                sub.id().equals("sub3") &&
                        sub.status() == SubscriptionStatus.EXPIRED
        ));
        verify(invoiceService, never()).generateInvoice(any());
    }

    @Test
    void processSubscription_ShouldDoNothingWhenNoInvoiceFound() {
        // Given
        Subscription subscription = expiredSubscription;
        when(invoiceService.getInvoice(subscription.id(), subscription.endDate())).thenReturn(null);

        // When
        scheduledTasks.processSubscription(subscription);

        // Then
        verify(subscriptionRepository, never()).save(any());
        verify(invoiceService, never()).generateInvoice(any());
    }

    @Test
    void renewSubscription_ShouldCreateNewInvoiceWithCorrectDetails() {
        // Given
        Subscription subscription = expiredSubscription;
        when(internetPlanRepository.findByIdInAndIsActive(any(), eq(true)))
                .thenReturn(List.of(new InternetPlan("plan3", "plan3","100Mbps", 99.99, "unlimited", true)));

        // When
        scheduledTasks.renewSubscription(subscription);

        // Then
        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(subscriptionCaptor.capture());

        Subscription renewedSubscription = subscriptionCaptor.getValue();
        assertEquals(SubscriptionStatus.ACTIVE, renewedSubscription.status());
        assertEquals(subscription.endDate().plus(Duration.ofDays(30)), renewedSubscription.endDate());

        ArgumentCaptor<InvoiceDTO> invoiceCaptor = ArgumentCaptor.forClass(InvoiceDTO.class);
        verify(invoiceService).generateInvoice(invoiceCaptor.capture());

        InvoiceDTO newInvoice = invoiceCaptor.getValue();
        assertEquals("cust3", newInvoice.customerId());
        assertEquals("sub3", newInvoice.subscriptionId());
        assertEquals(99.99, newInvoice.amountDue());
        assertEquals(renewedSubscription.endDate(), newInvoice.dueDate());
    }
}