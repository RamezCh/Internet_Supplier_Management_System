package com.github.ramezch.backend.customers.controllers;

import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    CustomerRepository repo;

    Customer newCustomer;

    String baseURL = "/api/customers";

    @BeforeEach
    void setup() {
        newCustomer = new Customer("new_customer", "New Customer", "test");
    }

    @Test
    @WithMockUser
    @DirtiesContext
    void getCustomers_whenExist_returnCustomers() throws Exception {
        // GIVEN
        repo.save(newCustomer);
        // WHEN
        mvc.perform(get(baseURL)
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "content": [
                                {
                                    "username": "new_customer",
                                    "fullName":  "New Customer",
                                    "notes": "test"
                                }
                            ]
                        }
                """));
    }

    @Test
    @WithMockUser
    @DirtiesContext
    void getCustomer_whenFound_returnCustomer() throws Exception {
        // GIVEN
        repo.save(newCustomer);
        // WHEN
        mvc.perform(get(baseURL + "/" + newCustomer.username())
                .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                        "username": "new_customer",
                        "fullName":  "New Customer",
                        "notes": "test"
                        }
                """));
    }

    @Test
    @WithMockUser
    @DirtiesContext
    void addCustomer_returnNewCustomer() throws Exception {
        // WHEN
        mvc.perform(post(baseURL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "username": "new_customer",
                  "fullName":  "New Customer",
                  "notes": "test"
                }
                """))
        // THEN
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                {
                  "username": "new_customer",
                  "fullName":  "New Customer",
                  "notes": "test"
                }
                """));
    }

    @Test
    @WithMockUser
    @DirtiesContext
    void updateCustomer_whenExist_returnNewCustomer() throws Exception {
        // GIVEN
        repo.save(newCustomer);
        // WHEN
        mvc.perform(put(baseURL + "/new_customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "new_customer",
                          "fullName":  "New Customer",
                          "notes": "test2"
                        }
                        """))
                // THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "username": "new_customer",
                          "fullName":  "New Customer",
                          "notes": "test2"
                        }
                        """));

    }

    @Test
    @WithMockUser
    @DirtiesContext
    void deleteTask_whenExist_returnNoContent() throws Exception {
        repo.save(newCustomer);

        // WHEN
        mvc.perform(delete(baseURL+"/new_customer")
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isNoContent());
    }
}