package com.github.ramezch.backend.customers.controllers;

import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    CustomerRepository repo;

    Customer newCustomer;

    @BeforeEach
    void setup() {
        newCustomer = new Customer("new_customer", "New Customer", "test");
    }

    @Test
    @WithMockUser
    void getCustomers() {
    }

    @Test
    @WithMockUser
    void getCustomer() {
    }

    @Test
    @WithMockUser
    void addCustomer() {
    }

    @Test
    @WithMockUser
    void updateCustomer() {
    }

    @Test
    @WithMockUser
    void deleteTask() {
    }
}