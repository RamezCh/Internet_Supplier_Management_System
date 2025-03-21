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
    void getCustomer_whenNotFound_returnNotFound() throws Exception {
        // WHEN
        mvc.perform(get(baseURL + "/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isNotFound());
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
    void addCustomer_whenInvalidData_returnBadRequest() throws Exception {
        // WHEN
        mvc.perform(post(baseURL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "username": "",
                  "fullName":  "",
                  "notes": "test"
                }
                """))
                // THEN
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DirtiesContext
    void updateCustomer_whenExist_returnUpdatedCustomer() throws Exception {
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
    void updateCustomer_whenNotFound_returnNotFound() throws Exception {
        // WHEN
        mvc.perform(put(baseURL + "/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "nonexistent",
                          "fullName":  "Nonexistent Customer",
                          "notes": "test"
                        }
                        """))
                // THEN
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DirtiesContext
    void deleteCustomer_whenExist_returnNoContent() throws Exception {
        repo.save(newCustomer);

        // WHEN
        mvc.perform(delete(baseURL+"/new_customer")
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteCustomer_whenNotExist_returnNotFound() throws Exception {
        // WHEN
        mvc.perform(delete(baseURL+"/new_customer")
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {
                        "message": "The Customer with username: new_customer could not be found."
                        }
                  """));
    }

    @Test
    @WithMockUser
    @DirtiesContext
    void getCustomers_whenNoCustomersExist_returnEmptyList() throws Exception {
        // WHEN
        mvc.perform(get(baseURL)
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "content": []
                        }
                """));
    }

    @Test
    @WithMockUser
    @DirtiesContext
    void addCustomer_whenUsernameAlreadyExists_returnConflict() throws Exception {
        // GIVEN
        repo.save(newCustomer);
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
                .andExpect(status().isConflict());
    }
}