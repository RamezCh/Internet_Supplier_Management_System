package com.github.ramezch.backend.internetplan.services;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.internetplan.models.InternetPlan;
import com.github.ramezch.backend.internetplan.models.InternetPlanDTO;
import com.github.ramezch.backend.internetplan.repositories.InternetPlanRepository;
import com.github.ramezch.backend.utils.IdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InternetPlanServiceTest {
    private InternetPlanRepository internetPlanRepo;
    private AppUserRepository appUserRepo;
    private IdService idService;
    private InternetPlanService internetPlanService;
    private InternetPlan internetPlan1, internetPlan2;
    private InternetPlanDTO internetPlanDTO1;
    private AppUser mockUser;

    @BeforeEach
    void setUp() {
        internetPlanRepo = mock(InternetPlanRepository.class);
        appUserRepo = mock(AppUserRepository.class);
        idService = mock(IdService.class);
        internetPlanService = new InternetPlanService(internetPlanRepo, appUserRepo, idService);
        internetPlan1 = new InternetPlan("1", "basic", "100Mbps", 72, "unlimited", true);
        internetPlan2 = new InternetPlan("2", "premium", "1000Mbps", 150, "unlimited", true);
        internetPlanDTO1 = new InternetPlanDTO( "premium", "1000Mbps", 250, "unlimited", true);
        mockUser = new AppUser();
        String userId = "user123";
        mockUser.setId(userId);
        when(appUserRepo.findById(userId)).thenReturn(Optional.of(mockUser));

    }

    @Test
    void getInternetPlans_returnInternetPlans_whenFound() {
        // GIVEN
        List<String> internetPlanIds = List.of("1", "2");
        mockUser.setInternetPlanIds(internetPlanIds);
        List<InternetPlan> expected = List.of(internetPlan1, internetPlan2);
        // WHEN
        when(internetPlanRepo.findAllById(internetPlanIds)).thenReturn(expected);
        List<InternetPlan> actual = internetPlanService.getInternetPlans(mockUser);
        // THEN
        assertEquals(expected, actual);
        verify(internetPlanRepo).findAllById(internetPlanIds);
    }

    @Test
    void getInternetPlans_returnEmptyList_whenNotFound() {
        // GIVEN
        mockUser.setInternetPlanIds(List.of());
        // WHEN
        List<InternetPlan> actual = internetPlanService.getInternetPlans(mockUser);
        // THEN
        assertThat(actual).isEmpty();
        verifyNoInteractions(internetPlanRepo);
    }

    @Test
    void getInternetPlan() {
    }

    @Test
    void addInternetPlan() {
    }

    @Test
    void updateInternetPlan() {
    }

    @Test
    void deleteInternetPlan() {
    }
}