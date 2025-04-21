package com.github.ramezch.backend.invoice.controllers;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.appuser.AppUserRoles;
import com.github.ramezch.backend.invoice.models.Invoice;
import com.github.ramezch.backend.invoice.repository.InvoiceRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InvoiceIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private InvoiceRepository invoiceRepo;
    @Autowired
    private AppUserRepository appUserRepo;

    private final String baseURL = "/api/invoices";
    private AppUser testUser;
    private Invoice testInvoice;

    @BeforeEach
    void setup() {
        // Clear repositories
        invoiceRepo.deleteAll();
        appUserRepo.deleteAll();

        // Setup test user
        testUser = new AppUser(
                "user123",
                "test_user",
                "test@example.com",
                List.of("cust123"), // Customer IDs this user has access to
                new ArrayList<>(),
                AppUserRoles.USER,
                Map.of(),
                List.of(new SimpleGrantedAuthority(AppUserRoles.USER.toString()))
        );
        appUserRepo.save(testUser);

        // Setup test invoice
        testInvoice = new Invoice(
                "inv123",
                "cust123",
                "sub123",
                Instant.now(),
                Instant.now().plusSeconds(86400),
                100.0,
                0,
                false
        );
    }

    @Test
    @DirtiesContext
    void getCustomerInvoices_whenExist_returnInvoices() throws Exception {
        // GIVEN
        invoiceRepo.save(testInvoice);

        // WHEN & THEN
        mvc.perform(get(baseURL + "/customer/cust123")
                        .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                [{
                    "id": "inv123",
                    "customerId": "cust123",
                    "subscriptionId": "sub123",
                    "amountDue": 100.0,
                    "amountPaid": 0,
                    "isPaid": false
                }]
            """));
    }

    @Test
    @DirtiesContext
    void getCustomerInvoices_whenNoInvoicesExist_returnEmptyList() throws Exception {
        // WHEN & THEN
        mvc.perform(get(baseURL + "/customer/cust123")
                        .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DirtiesContext
    void getInvoice_whenFound_returnInvoice() throws Exception {
        // GIVEN
        invoiceRepo.save(testInvoice);

        // WHEN & THEN
        mvc.perform(get(baseURL + "/inv123")
                        .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                {
                    "id": "inv123",
                    "customerId": "cust123",
                    "subscriptionId": "sub123",
                    "amountDue": 100.0,
                    "amountPaid": 0,
                    "isPaid": false
                }
            """));
    }

    @Test
    @DirtiesContext
    void getInvoice_whenNotFound_returnNotFound() throws Exception {
        // WHEN & THEN
        mvc.perform(get(baseURL + "/nonexistent")
                        .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Invoice with id: 'nonexistent' not found."));
    }

    @Test
    @DirtiesContext
    void updateInvoice_whenValidRequest_returnUpdatedInvoice() throws Exception {
        // GIVEN
        invoiceRepo.save(testInvoice);

        // WHEN & THEN
        mvc.perform(put(baseURL)
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "id": "inv123",
                        "amountPaid": 100.0
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    {
                        "id": "inv123",
                        "amountPaid": 100.0
                    }
                """));
    }

    @Test
    @DirtiesContext
    void updateInvoice_whenNotFound_returnNotFound() throws Exception {
        // WHEN & THEN
        mvc.perform(put(baseURL)
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "id": "nonexistent",
                        "amountPaid": 100.0
                    }
                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Invoice with id: 'nonexistent' not found."));
    }

    @Test
    @DirtiesContext
    void updateInvoice_whenInvalidData_returnBadRequest() throws Exception {
        // GIVEN
        invoiceRepo.save(testInvoice);

        // WHEN & THEN
        mvc.perform(put(baseURL)
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "id": "inv123",
                        "amountPaid": -100.0
                    }
                """))
                .andExpect(status().isBadRequest());
    }
}