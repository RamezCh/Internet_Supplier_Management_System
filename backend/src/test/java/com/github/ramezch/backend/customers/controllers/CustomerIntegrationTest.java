package com.github.ramezch.backend.customers.controllers;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.appuser.AppUserRoles;
import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private CustomerRepository repo;
    @Autowired
    private AppUserRepository appUserRepo;

    private Customer newCustomer;

    private final String baseURL = "/api/customers";

    private AppUser testUser;

    @BeforeEach
    void setup() {
        newCustomer = new Customer("new_customer", "New Customer", "test");
        testUser = new AppUser("123", "test_user", "w.com", List.of("new_customer"), AppUserRoles.USER, Map.of(), List.of(new SimpleGrantedAuthority(AppUserRoles.USER.toString())));
        appUserRepo.save(testUser);
    }

    @Test
    @DirtiesContext
    void getCustomers_whenExist_returnCustomers() throws Exception {
        // GIVEN
        repo.save(newCustomer);
        // WHEN
        mvc.perform(get(baseURL)
                        .with(oauth2Login().oauth2User(testUser))
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
    @DirtiesContext
    void getCustomer_whenFound_returnCustomer() throws Exception {
        // GIVEN
        repo.save(newCustomer);
        // WHEN
        mvc.perform(get(baseURL + "/" + newCustomer.username())
                        .with(oauth2Login().oauth2User(testUser))
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
    @DirtiesContext
    void getCustomer_whenNotFound_returnNotFound() throws Exception {
        // WHEN
        mvc.perform(get(baseURL + "/nonexistent")
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isNotFound());
    }

    @Test
    @DirtiesContext
    void addCustomer_returnNewCustomer() throws Exception {
        // WHEN
        mvc.perform(post(baseURL)
                        .with(oauth2Login().oauth2User(testUser))
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
    @DirtiesContext
    void addCustomer_whenInvalidData_returnBadRequest() throws Exception {
        // WHEN
        mvc.perform(post(baseURL)
                        .with(oauth2Login().oauth2User(testUser))
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
    @DirtiesContext
    void updateCustomer_whenExist_returnUpdatedCustomer() throws Exception {
        // GIVEN
        repo.save(newCustomer);
        // WHEN
        mvc.perform(put(baseURL + "/new_customer")
                        .with(oauth2Login().oauth2User(testUser))
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
    @DirtiesContext
    void updateCustomer_whenNotFound_returnNotFound() throws Exception {
        // WHEN
        mvc.perform(put(baseURL + "/nonexistent")
                        .with(oauth2Login().oauth2User(testUser))
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
    @DirtiesContext
    void deleteCustomer_whenExist_returnNoContent() throws Exception {
        repo.save(newCustomer);

        // WHEN
        mvc.perform(delete(baseURL+"/new_customer")
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCustomer_whenNotExist_returnNotFound() throws Exception {
        // WHEN
        mvc.perform(delete(baseURL+"/new_customer2")
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {
                        "message": "The Customer with username: 'new_customer2' could not be found."
                        }
                  """));
    }

    @Test
    @DirtiesContext
    void getCustomers_whenNoCustomersExist_returnEmptyList() throws Exception {
        // WHEN
        mvc.perform(get(baseURL)
                        .with(oauth2Login().oauth2User(testUser))
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
    @DirtiesContext
    void addCustomer_whenUsernameAlreadyExists_returnConflict() throws Exception {
        // GIVEN
        repo.save(newCustomer);
        // WHEN
        mvc.perform(post(baseURL)
                        .with(oauth2Login().oauth2User(testUser))
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