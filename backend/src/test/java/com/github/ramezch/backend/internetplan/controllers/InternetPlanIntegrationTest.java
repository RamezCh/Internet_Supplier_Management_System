package com.github.ramezch.backend.internetplan.controllers;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.appuser.AppUserRoles;
import com.github.ramezch.backend.internetplan.models.InternetPlan;
import com.github.ramezch.backend.internetplan.repositories.InternetPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InternetPlanIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private InternetPlanRepository repo;
    @Autowired
    private AppUserRepository appUserRepo;

    private final String baseURL = "/api/internet_plans";
    private AppUser testUser;
    private InternetPlan testPlan;

    @BeforeEach
    void setup() {
        testUser = new AppUser(
                "user123",
                "test_user",
                "test@example.com",
                new ArrayList<>(),
                new ArrayList<>(),
                AppUserRoles.USER,
                Map.of(),
                List.of(new SimpleGrantedAuthority(AppUserRoles.USER.toString()))
        );
        appUserRepo.save(testUser);

        testPlan = new InternetPlan(
                "plan123",
                "Premium Plan",
                "1000Mbps",
                99.99,
                "unlimited",
                true
        );
    }

    @Test
    @DirtiesContext
    void getInternetPlans_whenExist_returnPlans() throws Exception {
        // GIVEN
        testUser.setInternetPlanIds(List.of("plan123"));
        appUserRepo.save(testUser);
        repo.save(testPlan);

        // WHEN & THEN
        mvc.perform(get(baseURL)
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                [{
                    "id": "plan123",
                    "name": "Premium Plan",
                    "speed": "1000Mbps",
                    "price": 99.99,
                    "bandwidth": "unlimited",
                    "isActive": true
                }]
            """));
    }

    @Test
    @DirtiesContext
    void getInternetPlans_whenNoPlansExist_returnEmptyList() throws Exception {
        // WHEN & THEN
        mvc.perform(get(baseURL)
                .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DirtiesContext
    void getInternetPlan_whenFound_returnPlan() throws Exception {
        // GIVEN
        testUser.setInternetPlanIds(List.of("plan123"));
        appUserRepo.save(testUser);
        repo.save(testPlan);

        // WHEN & THEN
        mvc.perform(get(baseURL + "/plan123")
                        .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                {
                    "id": "plan123",
                    "name": "Premium Plan",
                    "speed": "1000Mbps",
                    "price": 99.99,
                    "bandwidth": "unlimited",
                    "isActive": true
                }
            """));
    }

    @Test
    @DirtiesContext
    void getInternetPlan_whenNotFound_returnNotFound() throws Exception {
        // WHEN & THEN
        mvc.perform(get(baseURL + "/nonexistent")
                        .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Internet plan with id: 'nonexistent' not found."));
    }

    @Test
    @DirtiesContext
    void addInternetPlan_returnNewPlan() throws Exception {
        // WHEN & THEN
        mvc.perform(post(baseURL)
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "name": "Basic Plan",
                        "speed": "100Mbps",
                        "price": 49.99,
                        "bandwidth": "500GB",
                        "isActive": true
                    }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Basic Plan"))
                .andExpect(jsonPath("$.speed").value("100Mbps"))
                .andExpect(jsonPath("$.price").value(49.99))
                .andExpect(jsonPath("$.bandwidth").value("500GB"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DirtiesContext
    void addInternetPlan_whenNameExists_returnConflict() throws Exception {
        // GIVEN
        testUser.setInternetPlanIds(List.of("plan123"));
        appUserRepo.save(testUser);
        repo.save(testPlan);

        // WHEN & THEN
        mvc.perform(post(baseURL)
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "name": "Premium Plan",
                        "speed": "200Mbps",
                        "price": 59.99,
                        "bandwidth": "1TB",
                        "isActive": true
                    }
                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Internet plan name 'Premium Plan' is already taken."));
    }

    @Test
    @DirtiesContext
    void addInternetPlan_whenInvalidData_returnBadRequest() throws Exception {
        // WHEN & THEN
        mvc.perform(post(baseURL)
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "name": "",
                        "speed": "",
                        "price": -1,
                        "bandwidth": "",
                        "isActive": true
                    }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext
    void updateInternetPlan_whenExists_returnUpdatedPlan() throws Exception {
        // GIVEN
        testUser.setInternetPlanIds(List.of("plan123"));
        appUserRepo.save(testUser);
        repo.save(testPlan);

        // WHEN & THEN
        mvc.perform(put(baseURL + "/plan123")
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "name": "Updated Plan",
                        "speed": "500Mbps",
                        "price": 79.99,
                        "bandwidth": "2TB",
                        "isActive": false
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                {
                    "id": "plan123",
                    "name": "Updated Plan",
                    "speed": "500Mbps",
                    "price": 79.99,
                    "bandwidth": "2TB",
                    "isActive": false
                }
            """));
    }

    @Test
    @DirtiesContext
    void updateInternetPlan_whenNotFound_returnNotFound() throws Exception {
        // WHEN & THEN
        mvc.perform(put(baseURL + "/nonexistent")
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "name": "New Plan",
                        "speed": "100Mbps",
                        "price": 49.99,
                        "bandwidth": "500GB",
                        "isActive": true
                    }
                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Internet plan with id: 'nonexistent' not found."));
    }

    @Test
    @DirtiesContext
    void deleteInternetPlan_whenExists_returnNoContent() throws Exception {
        // GIVEN
        testUser.setInternetPlanIds(new ArrayList<>(List.of("plan123")));
        appUserRepo.save(testUser);
        repo.save(testPlan);

        // WHEN & THEN
        mvc.perform(delete(baseURL + "/plan123")
                        .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DirtiesContext
    void deleteInternetPlan_whenNotExists_returnNotFound() throws Exception {
        // WHEN & THEN
        mvc.perform(delete(baseURL + "/nonexistent")
                        .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Internet plan with id: 'nonexistent' not found."));
    }

    @Test
    @DirtiesContext
    void getActivePlansByUser_whenActivePlansExist_returnActivePlans() throws Exception {
        // GIVEN
        testUser.setInternetPlanIds(List.of("plan123"));
        appUserRepo.save(testUser);
        repo.save(testPlan);

        // WHEN & THEN
        mvc.perform(get(baseURL + "/small")
                        .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().json("""
            [{
                "id": "plan123",
                "name": "Premium Plan"
            }]
        """));
    }

    @Test
    @DirtiesContext
    void getActivePlansByUser_whenNoActivePlans_returnEmptyList() throws Exception {
        // GIVEN
        InternetPlan inactivePlan = new InternetPlan(
                "plan456",
                "Inactive Plan",
                "500Mbps",
                79.99,
                "1TB",
                false
        );
        testUser.setInternetPlanIds(List.of("plan456"));
        appUserRepo.save(testUser);
        repo.save(inactivePlan);

        // WHEN & THEN
        mvc.perform(get(baseURL + "/small")
                        .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DirtiesContext
    void getActivePlansByUser_whenNoPlans_returnEmptyList() throws Exception {
        // WHEN & THEN
        mvc.perform(get(baseURL + "/small")
                        .with(oauth2Login().oauth2User(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DirtiesContext
    void updateInternetPlan_whenNameExists_returnConflict() throws Exception {
        // GIVEN
        InternetPlan existingPlan = new InternetPlan(
                "plan456",
                "Existing Plan",
                "500Mbps",
                79.99,
                "1TB",
                true
        );
        testUser.setInternetPlanIds(List.of("plan123", "plan456"));
        appUserRepo.save(testUser);
        repo.save(testPlan);
        repo.save(existingPlan);

        // WHEN & THEN
        mvc.perform(put(baseURL + "/plan123")
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "name": "Existing Plan",
                    "speed": "500Mbps",
                    "price": 79.99,
                    "bandwidth": "1TB",
                    "isActive": true
                }
            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Internet plan name 'Existing Plan' is already taken."));
    }

    @Test
    @DirtiesContext
    void addInternetPlan_whenUnauthorized_returnUnauthorized() throws Exception {
        // WHEN & THEN
        mvc.perform(post(baseURL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "name": "Basic Plan",
                    "speed": "100Mbps",
                    "price": 49.99,
                    "bandwidth": "500GB",
                    "isActive": true
                }
            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DirtiesContext
    void updateInternetPlan_whenInvalidData_returnBadRequest() throws Exception {
        // GIVEN
        testUser.setInternetPlanIds(List.of("plan123"));
        appUserRepo.save(testUser);
        repo.save(testPlan);

        // WHEN & THEN
        mvc.perform(put(baseURL + "/plan123")
                        .with(oauth2Login().oauth2User(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "name": "",
                    "speed": "",
                    "price": -1,
                    "bandwidth": "",
                    "isActive": true
                }
            """))
                .andExpect(status().isBadRequest());
    }
}