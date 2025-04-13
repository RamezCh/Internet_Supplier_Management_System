package com.github.ramezch.backend.subscription.controllers;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.appuser.AppUserRoles;
import com.github.ramezch.backend.customers.models.Address;
import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.models.CustomerStatus;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import com.github.ramezch.backend.internetplan.models.InternetPlan;
import com.github.ramezch.backend.internetplan.repositories.InternetPlanRepository;
import com.github.ramezch.backend.subscription.models.Subscription;
import com.github.ramezch.backend.subscription.models.SubscriptionStatus;
import com.github.ramezch.backend.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SubscriptionControllerIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private SubscriptionRepository subscriptionRepo;

    @Autowired
    private InternetPlanRepository internetPlanRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private AppUserRepository appUserRepo;

    private final String baseURL = "/api/subscriptions";
    private AppUser testUser;
    private Subscription testSubscription;

    @BeforeEach
    void setup() {
        testUser = new AppUser(
                "user123",
                "test_user",
                "test@example.com",
                List.of("customer123"), // Add customer ID
                new ArrayList<>(),
                AppUserRoles.USER,
                Map.of(),
                List.of(new SimpleGrantedAuthority(AppUserRoles.USER.toString()))
        );
        appUserRepo.save(testUser);

        InternetPlan testInternetPlan = new InternetPlan(
                "plan123",
                "Premium Plan",
                "1000Mbps",
                99.99,
                "unlimited",
                true
        );
        internetPlanRepo.save(testInternetPlan);

        Address address = new Address("123 Main St", "Springfield", "IL", "62704", "USA");
        Customer testCustomer = new Customer(
                "customer123",
                "test_customer",
                "Test Customer",
                "123456789",
                address,
                Instant.now(),
                CustomerStatus.ACTIVE,
                "test@example.com"
        );
        customerRepo.save(testCustomer);

        Instant startDate = Instant.now();
        Instant endDate = startDate.plus(30, ChronoUnit.DAYS);

        testSubscription = new Subscription(
                "sub123",
                "customer123",
                "plan123",
                startDate,
                endDate,
                SubscriptionStatus.ACTIVE
        );
    }

    @Test
    @DirtiesContext
    void getSubscription_whenExists_returnsSubscriptionDetails() throws Exception {
        // GIVEN
        subscriptionRepo.save(testSubscription);

        // WHEN & THEN
        mvc.perform(get(baseURL + "/customer123")
                        .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("sub123"))
                .andExpect(jsonPath("$.customer.id").value("customer123"))
                .andExpect(jsonPath("$.customer.fullName").value("Test Customer"))
                .andExpect(jsonPath("$.internetPlan.id").value("plan123"))
                .andExpect(jsonPath("$.internetPlan.name").value("Premium Plan"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.startDate").exists())
                .andExpect(jsonPath("$.endDate").exists());
    }

    @Test
    @DirtiesContext
    void getSubscription_whenNotExists_returnsNotFound() throws Exception {
        // WHEN & THEN
        mvc.perform(get(baseURL + "/nonexistent")
                        .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The Customer with username: 'nonexistent' could not be found."));
    }

    @Test
    @DirtiesContext
    void getSubscription_whenDifferentAppUserCustomer_returnsNotFound() throws Exception {
        // GIVEN
        AppUser otherUser = new AppUser(
                "user456",
                "other_user",
                "other@example.com",
                List.of("otherCustomer"),
                new ArrayList<>(),
                AppUserRoles.USER,
                Map.of(),
                List.of(new SimpleGrantedAuthority(AppUserRoles.USER.toString()))
        );
        appUserRepo.save(otherUser);
        subscriptionRepo.save(testSubscription);

        // WHEN & THEN
        mvc.perform(get(baseURL + "/customer123")
                        .with(oauth2Login().oauth2User(otherUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The Customer with username: 'customer123' could not be found."));
    }

    @Test
    @DirtiesContext
    void updateSubscription_whenValid_returnsUpdatedSubscription() throws Exception {
        // GIVEN
        subscriptionRepo.save(testSubscription);
        Instant newEndDate = Instant.now().plus(60, ChronoUnit.DAYS);

        // WHEN & THEN
        mvc.perform(put(baseURL + "/customer123")
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "customerId": "customer123",
                        "internetPlanId": "plan123",
                        "startDate": "%s",
                        "endDate": "%s",
                        "status": "CANCELLED"
                    }
                    """.formatted(testSubscription.startDate().toString(), newEndDate.toString())))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    {
                        "id": "sub123",
                        "customerId": "customer123",
                        "internetPlanId": "plan123",
                        "status": "CANCELLED"
                    }
                    """))
                .andExpect(jsonPath("$.startDate").exists())
                .andExpect(jsonPath("$.endDate").exists());
    }

    @Test
    @DirtiesContext
    void updateSubscription_whenNotExists_returnsNotFound() throws Exception {
        // WHEN & THEN
        mvc.perform(put(baseURL + "/nonexistent")
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "customerId": "nonexistent",
                            "internetPlanId": "plan123",
                            "startDate": "%s",
                            "endDate": "%s",
                            "status": "ACTIVE"
                        }
                        """.formatted(Instant.now().toString(), Instant.now().plus(30, ChronoUnit.DAYS).toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The Customer with username: 'nonexistent' could not be found."));
    }

    @Test
    @DirtiesContext
    void updateSubscription_whenInvalidData_returnsBadRequest() throws Exception {
        // GIVEN
        subscriptionRepo.save(testSubscription);

        // WHEN & THEN
        mvc.perform(put(baseURL + "/customer123")
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "customerId": "",
                            "internetPlanId": "",
                            "startDate": null,
                            "endDate": null,
                            "status": "INVALID"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext
    void deleteSubscription_whenExists_returnsNoContent() throws Exception {
        // GIVEN
        subscriptionRepo.save(testSubscription);

        // WHEN & THEN
        mvc.perform(delete(baseURL + "/customer123")
                        .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isOk());
    }

    @Test
    @DirtiesContext
    void deleteSubscription_whenNotExists_returnsNotFound() throws Exception {
        // WHEN & THEN
        mvc.perform(delete(baseURL + "/nonexistent")
                        .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The Customer with username: 'nonexistent' could not be found."));
    }

    @Test
    @DirtiesContext
    void deleteSubscription_whenUnauthorizedCustomer_returnsForbidden() throws Exception {
        // GIVEN
        AppUser otherUser = new AppUser(
                "user456",
                "other_user",
                "other@example.com",
                List.of("otherCustomer"),
                new ArrayList<>(),
                AppUserRoles.USER,
                Map.of(),
                List.of(new SimpleGrantedAuthority(AppUserRoles.USER.toString()))
        );
        appUserRepo.save(otherUser);
        subscriptionRepo.save(testSubscription);

        // WHEN & THEN
        mvc.perform(delete(baseURL + "/customer123")
                        .with(oauth2Login().oauth2User(otherUser)))
                .andExpect(jsonPath("$.message").value("The Customer with username: 'customer123' could not be found."));
    }
}