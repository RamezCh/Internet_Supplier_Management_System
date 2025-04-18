package com.github.ramezch.backend.customers.controllers;

import com.github.ramezch.backend.customers.models.Address;
import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.appuser.AppUserRoles;
import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.models.CustomerStatus;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import com.github.ramezch.backend.internetplan.models.InternetPlan;
import com.github.ramezch.backend.internetplan.repositories.InternetPlanRepository;
import com.github.ramezch.backend.subscription.models.Subscription;
import com.github.ramezch.backend.subscription.models.SubscriptionStatus;
import com.github.ramezch.backend.subscription.repository.SubscriptionRepository;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @Autowired
    private InternetPlanRepository internetPlanRepo;
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private Customer newCustomer;
    private final String baseURL = "/api/customers";
    private AppUser testUser;

    @BeforeEach
    void setup() {
        Instant now = Instant.now();
        Address address = new Address(idService.randomId(),"Deutschland", "Berlin", "BeispielStrasse", "10000");
        newCustomer = new Customer("123","new_customer", "New Customer", "78863120", address, now, CustomerStatus.PENDING_ACTIVATION, "test");
        testUser = new AppUser("123", "test_user", "w.com", new ArrayList<>(List.of("123")),new ArrayList<>(List.of("")), AppUserRoles.USER, Map.of(), List.of(new SimpleGrantedAuthority(AppUserRoles.USER.toString())));
        appUserRepo.save(testUser);
        InternetPlan internetPlan = new InternetPlan("1", "basic", "100Mbps", 75, "unlimited", true);
        internetPlanRepo.save(internetPlan);
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
                                    "status": "PENDING_ACTIVATION",
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
                            "status": "PENDING_ACTIVATION",
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
                        .param("internetPlanId", "1")
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
                        .param("internetPlanId", "1")
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
        Instant startDate = Instant.now();
        Instant endDate = Instant.now();
        Subscription newCustomerSub = new Subscription("23456", "123", "1", startDate, endDate, SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(newCustomerSub);
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
                        .param("internetPlanId", "1")
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

    @Test
    @DirtiesContext
    void searchCustomers_withStatusOnly_returnsFilteredCustomers() throws Exception {
        // GIVEN
        Customer activeCustomer = new Customer("active123", "active_user", "Active User", "12345678",
                newCustomer.address(), Instant.now(), CustomerStatus.ACTIVE, "active notes");
        repo.saveAll(List.of(newCustomer, activeCustomer));
        testUser.setCustomerIds(List.of("123", "active123"));

        // WHEN
        mvc.perform(get(baseURL + "/search")
                .param("status", "PENDING_ACTIVATION")
                .with(oauth2Login().oauth2User(testUser)))
                // THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    {
                        "content": [
                            {
                                "id": "123",
                                "username": "new_customer",
                                "fullName": "New Customer",
                                "status": "PENDING_ACTIVATION"
                            }
                        ],
                        "totalElements": 1
                    }
            """));
    }

    @Test
    @DirtiesContext
    void searchCustomers_withSearchTermOnly_returnsMatchingCustomers() throws Exception {
        // GIVEN
        Customer usernameMatch = new Customer("124326", "hamburg_user", "User", "87654321",
                new Address(idService.randomId(), "Deutschland", "Hamburg", "OtherStreet", "10115"),
                Instant.now(), CustomerStatus.ACTIVE, "notes");
        Customer fullNameMatch = new Customer("987654", "dsfgh", "Hamburg User", "87654321",
                new Address(idService.randomId(), "Deutschland", "dfgh", "OtherStreet", "10115"),
                Instant.now(), CustomerStatus.ACTIVE, "notes");
        Customer cityMatch = new Customer("34567865", "dsfghj", "CDUser", "87654321",
                new Address(idService.randomId(), "Deutschland", "Hamburg", "OtherStreet", "10115"),
                Instant.now(), CustomerStatus.ACTIVE, "notes");

        repo.saveAll(List.of(usernameMatch, fullNameMatch, cityMatch));
        testUser.setCustomerIds(List.of(usernameMatch.id(), fullNameMatch.id(), cityMatch.id()));

        // WHEN
        mvc.perform(get(baseURL + "/search")
                        .param("searchTerm", "Hamburg")
                        .with(oauth2Login().oauth2User(testUser)))
                // THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                {
                    "content": [
                        {
                            "id": "124326",
                            "username": "hamburg_user",
                            "fullName": "User",
                            "address": {
                                "city": "Hamburg"
                            }
                        },
                        {
                            "id": "987654",
                            "username": "dsfgh",
                            "fullName": "Hamburg User",
                            "address": {
                                "city": "dfgh"
                            }
                        },
                        {
                            "id": "34567865",
                            "username": "dsfghj",
                            "fullName": "CDUser",
                            "address": {
                                "city": "Hamburg"
                            }
                        }
                    ],
                    "totalElements": 3
                }
            """));
    }

    @Test
    @DirtiesContext
    void searchCustomers_withStatusAndSearchTerm_returnsMatchingCustomers() throws Exception {
        // GIVEN
        Customer pendingBerlinCustomer = new Customer("pendingBerlin", "pending_berlin", "Pending Berlin", "11111111",
                new Address(idService.randomId(), "Deutschland", "Berlin", "PendingStrasse", "10115"),
                Instant.now(), CustomerStatus.PENDING_ACTIVATION, "pending berlin");
        repo.saveAll(List.of(newCustomer, pendingBerlinCustomer));
        testUser.setCustomerIds(List.of("123", "pendingBerlin"));

        // WHEN
        mvc.perform(get(baseURL + "/search")
                        .param("status", "PENDING_ACTIVATION")
                        .param("searchTerm", "Berlin")
                        .with(oauth2Login().oauth2User(testUser)))
                // THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    {
                        "content":
                        [{
                            "id":"123",
                            "username":"new_customer",
                            "fullName":"New Customer",
                            "phone":"78863120",
                            "address":{
                                "country":"Deutschland",
                                "city":"Berlin",
                                "street":"BeispielStrasse",
                                "postalCode":"10000"
                            },
                            "status":"PENDING_ACTIVATION",
                            "notes":"test"
                            },
                            {
                            "id":"pendingBerlin",
                            "username":"pending_berlin",
                            "fullName":"Pending Berlin",
                            "phone":"11111111",
                            "address":{
                                "country":"Deutschland",
                                "city":"Berlin",
                                "street":"PendingStrasse",
                                "postalCode":"10115"
                            },
                            "status":"PENDING_ACTIVATION",
                            "notes":"pending berlin"
                        }],
                        "totalElements":2,
                        "totalPages":1
                    }
            """));
    }

    @Test
    @DirtiesContext
    void searchCustomers_withNoParams_returnsAllCustomers() throws Exception {
        // GIVEN
        Customer anotherCustomer = new Customer("another123", "another_user", "Another User", "22222222",
                newCustomer.address(), Instant.now(), CustomerStatus.ACTIVE, "another notes");
        repo.saveAll(List.of(newCustomer, anotherCustomer));
        testUser.setCustomerIds(List.of("123", "another123"));

        // WHEN
        mvc.perform(get(baseURL + "/search")
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].username").value("another_user"))
                .andExpect(jsonPath("$.content[1].username").value("new_customer"));
    }

    @Test
    @DirtiesContext
    void searchCustomers_withNoMatchingResults_returnsEmptyPage() throws Exception {
        // GIVEN
        repo.save(newCustomer);
        testUser.setCustomerIds(List.of("123"));

        // WHEN
        mvc.perform(get(baseURL + "/search")
                        .param("searchTerm", "nonexistent")
                        .with(oauth2Login().oauth2User(testUser)))
                // THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    {
                        "content": [],
                        "totalElements": 0
                    }
            """));
    }

    @Test
    @DirtiesContext
    void searchCustomers_withEmptyCustomerIds_returnsEmptyPage() throws Exception {
        // GIVEN
        repo.save(newCustomer);
        testUser.setCustomerIds(List.of());

        // WHEN
        mvc.perform(get(baseURL + "/search")
                        .with(oauth2Login().oauth2User(testUser)))
                // THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    {
                        "content": [],
                        "totalElements": 0
                    }
            """));
    }

    @Test
    @DirtiesContext
    void searchCustomers_withPagination_returnsCorrectPage() throws Exception {
        // GIVEN
        Customer customer2 = new Customer("234", "customer2", "Customer Two", "22222222",
                newCustomer.address(), Instant.now(), CustomerStatus.ACTIVE, "notes2");
        Customer customer3 = new Customer("345", "customer3", "Customer Three", "33333333",
                newCustomer.address(), Instant.now(), CustomerStatus.ACTIVE, "notes3");
        repo.saveAll(List.of(newCustomer, customer2, customer3));
        testUser.setCustomerIds(List.of("123", "234", "345"));

        // WHEN - Request second page with 1 item per page
        mvc.perform(get(baseURL + "/search")
                        .param("page", "1")
                        .param("size", "1")
                        .with(oauth2Login().oauth2User(testUser)))
                // THEN
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    {
                        "content": [
                            {
                                "id": "234",
                                "username": "customer2"
                            }
                        ],
                        "totalElements": 3,
                        "number": 1,
                        "size": 1
                    }
            """));
    }
}