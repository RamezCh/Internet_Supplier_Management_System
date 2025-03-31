package com.github.ramezch.backend.customers.controllers;

import com.github.ramezch.backend.customers.models.Address;
import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.appuser.AppUserRoles;
import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.models.CustomerStatus;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import com.github.ramezch.backend.utils.IdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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
    @Autowired
    private IdService idService;

    private Customer newCustomer;
    private final String baseURL = "/api/customers";
    private AppUser testUser;

    @BeforeEach
    void setup() {
        LocalDate now = LocalDate.now();
        Address address = new Address(idService.randomId(),"Deutschland", "Berlin", "BeispielStrasse", "10000");
        newCustomer = new Customer("123","new_customer", "New Customer", "78863120", address, now, CustomerStatus.PENDING_ACTIVATION, "test");
        testUser = new AppUser("123", "test_user", "w.com", List.of("123"), AppUserRoles.USER, Map.of(), List.of(new SimpleGrantedAuthority(AppUserRoles.USER.toString())));
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
                                    "id": "123",
                                    "username": "new_customer",
                                    "fullName": "New Customer",
                                    "phone": "78863120",
                                    "address": {
                                        "country": "Deutschland",
                                        "city": "Berlin",
                                        "street": "BeispielStrasse",
                                        "postalCode": "10000"
                                    },
                                    "registrationDate": "%s",
                                    "status": "PENDING_ACTIVATION",
                                    "notes": "test"
                                }
                            ]
                        }
                """.formatted(LocalDate.now())));
    }

    @Test
    @DirtiesContext
    void getCustomer_whenFound_returnCustomer() throws Exception {
        // GIVEN
        repo.save(newCustomer);
        // WHEN
        mvc.perform(get(baseURL + "/" + newCustomer.id())
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "id": "123",
                            "username": "new_customer",
                            "fullName": "New Customer",
                            "phone": "78863120",
                            "address": {
                                "country": "Deutschland",
                                "city": "Berlin",
                                "street": "BeispielStrasse",
                                "postalCode": "10000"
                            },
                            "registrationDate": "%s",
                            "status": "PENDING_ACTIVATION",
                            "notes": "test"
                        }
                """.formatted(LocalDate.now())));
    }

    @Test
    @DirtiesContext
    void getCustomer_whenNotFound_returnNotFound() throws Exception {
        // WHEN
        mvc.perform(get(baseURL + "/nonexistent")
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {
                            "message": "The Customer with username: 'nonexistent' could not be found."
                        }
                """));
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
                                    "fullName": "New Customer",
                                    "phone": "78863120",
                                    "address": {
                                        "country": "Deutschland",
                                        "city": "Berlin",
                                        "street": "BeispielStrasse",
                                        "postalCode": "10000"
                                    },
                                    "status": "PENDING_ACTIVATION",
                                    "notes": "test"
                                }
                """))
                // THEN
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                            "username": "new_customer",
                            "fullName": "New Customer",
                            "phone": "78863120",
                            "address": {
                                "country": "Deutschland",
                                "city": "Berlin",
                                "street": "BeispielStrasse",
                                "postalCode": "10000"
                            },
                            "status": "PENDING_ACTIVATION",
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
                                    "fullName": "",
                                    "phone": "",
                                    "address": {
                                        "country": "",
                                        "city": "",
                                        "street": "",
                                        "postalCode": ""
                                    },
                                    "status": "PENDING_ACTIVATION",
                                    "notes": ""
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
        mvc.perform(put(baseURL + "/123")
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "123",
                                    "username": "updated_customer",
                                    "fullName": "Updated Customer",
                                    "phone": "78863121",
                                    "address": {
                                        "country": "Deutschland",
                                        "city": "Berlin",
                                        "street": "UpdatedStrasse",
                                        "postalCode": "10001"
                                    },
                                    "status": "ACTIVE",
                                    "notes": "updated notes"
                                }
                        """))
                // THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "id": "123",
                            "username": "updated_customer",
                            "fullName": "Updated Customer",
                            "phone": "78863121",
                            "address": {
                                "country": "Deutschland",
                                "city": "Berlin",
                                "street": "UpdatedStrasse",
                                "postalCode": "10001"
                            },
                            "status": "ACTIVE",
                            "notes": "updated notes"
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
                                    "id": "nonexistent",
                                    "username": "new_customer",
                                    "fullName": "New Customer",
                                    "phone": "78863120",
                                    "address": {
                                        "country": "Deutschland",
                                        "city": "Berlin",
                                        "street": "BeispielStrasse",
                                        "postalCode": "10000"
                                    },
                                    "status": "PENDING_ACTIVATION",
                                    "notes": "test"
                                }
                        """))
                // THEN
                .andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {
                            "message": "The Customer with username: 'nonexistent' could not be found."
                        }
                """));
    }

    @Test
    @DirtiesContext
    void deleteCustomer_whenExist_returnNoContent() throws Exception {
        // GIVEN
        repo.save(newCustomer);
        // WHEN
        mvc.perform(delete(baseURL + "/123")
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isNoContent());
    }

    @Test
    @DirtiesContext
    void deleteCustomer_whenNotExist_returnNotFound() throws Exception {
        // WHEN
        mvc.perform(delete(baseURL + "/nonexistent")
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {
                            "message": "The Customer with username: 'nonexistent' could not be found."
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
                                    "fullName": "New Customer",
                                    "phone": "78863120",
                                    "address": {
                                        "country": "Deutschland",
                                        "city": "Berlin",
                                        "street": "BeispielStrasse",
                                        "postalCode": "10000"
                                    },
                                    "status": "PENDING_ACTIVATION",
                                    "notes": "test"
                                }
                        """))
                // THEN
                .andExpect(status().isConflict())
                .andExpect(content().json("""
                        {
                            "message": "The Customer with username: 'new_customer' already exists."
                        }
                """));
    }
}