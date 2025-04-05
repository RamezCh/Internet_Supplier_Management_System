package com.github.ramezch.backend.internetplan.services;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.exceptions.InternetPlanNameTakenException;
import com.github.ramezch.backend.exceptions.InternetPlanNotFoundException;
import com.github.ramezch.backend.internetplan.models.InternetPlan;
import com.github.ramezch.backend.internetplan.models.InternetPlanDTO;
import com.github.ramezch.backend.internetplan.repositories.InternetPlanRepository;
import com.github.ramezch.backend.utils.IdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
    private InternetPlanDTO internetPlanDTO1, internetPlanDTO2;
    private AppUser mockUser;

    @BeforeEach
    void setUp() {
        internetPlanRepo = mock(InternetPlanRepository.class);
        appUserRepo = mock(AppUserRepository.class);
        idService = mock(IdService.class);
        internetPlanService = new InternetPlanService(internetPlanRepo, appUserRepo, idService);
        internetPlan1 = new InternetPlan("1", "basic", "100Mbps", 72, "unlimited", true);
        internetPlan2 = new InternetPlan("2", "premium", "1000Mbps", 150, "unlimited", true);
        internetPlanDTO1 = new InternetPlanDTO("premium", "1000Mbps", 250, "unlimited", true);
        internetPlanDTO2 = new InternetPlanDTO("basic", "100Mbps", 72, "unlimited", true);
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
    void getInternetPlan_whenFound_returnOptionalOfInternetPlan() {
        // GIVEN
        mockUser.setInternetPlanIds(List.of("1"));
        // WHEN
        when(internetPlanRepo.findById("1")).thenReturn(Optional.ofNullable(internetPlan1));
        Optional<InternetPlan> actual = internetPlanService.getInternetPlan("1", mockUser);
        // THEN
        assertEquals(Optional.ofNullable(internetPlan1), actual);
        verify(internetPlanRepo).findById("1");
    }

    @Test
    void getInternetPlan_whenNotFound_returnEmptyOptional() {
        // GIVEN
        mockUser.setInternetPlanIds(List.of("1"));
        // WHEN
        Optional<InternetPlan> actual = internetPlanService.getInternetPlan("9", mockUser);
        // THEN
        assertEquals(Optional.empty(), actual);
        verify(internetPlanRepo, never()).findById("9");
    }

    @Test
    void addInternetPlan_shouldAddNewPlanSuccessfully() {
        // GIVEN
        mockUser.setInternetPlanIds(new ArrayList<>());
        when(idService.randomId()).thenReturn("new-id");
        when(internetPlanRepo.existsByNameAndIdIn("premium", new ArrayList<>())).thenReturn(false);
        when(internetPlanRepo.existsById("new-id")).thenReturn(false);

        // WHEN
        InternetPlan result = internetPlanService.addInternetPlan(internetPlanDTO1, mockUser);

        // THEN
        assertNotNull(result);
        assertEquals("new-id", result.id());
        assertEquals("premium", result.name());
        assertTrue(result.isActive());

        verify(internetPlanRepo).save(result);
        verify(appUserRepo).save(mockUser);
        assertTrue(mockUser.getInternetPlanIds().contains("new-id"));
    }

    @Test
    void addInternetPlan_shouldThrowException_whenNameExists() {
        // GIVEN
        List<String> existingPlanIds = List.of("1");
        mockUser.setInternetPlanIds(new ArrayList<>(existingPlanIds));
        when(internetPlanRepo.existsByNameAndIdIn("basic", existingPlanIds)).thenReturn(true);

        // WHEN & THEN
        assertThrows(InternetPlanNameTakenException.class, () -> internetPlanService.addInternetPlan(internetPlanDTO2, mockUser));

        verify(internetPlanRepo, never()).save(any());
        verify(appUserRepo, never()).save(any());
    }

    @Test
    void updateInternetPlan_shouldUpdatePlanSuccessfully() {
        // GIVEN
        mockUser.setInternetPlanIds(List.of("1"));
        when(internetPlanRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        InternetPlan result = internetPlanService.updateInternetPlan("1", internetPlanDTO1, mockUser);

        // THEN
        assertNotNull(result);
        assertEquals("1", result.id());
        assertEquals("premium", result.name());
        assertTrue(result.isActive());

        verify(internetPlanRepo).save(result);
    }

    @Test
    void updateInternetPlan_shouldThrowException_whenPlanNotFound() {
        // GIVEN
        mockUser.setInternetPlanIds(List.of("1"));

        // WHEN & THEN
        assertThrows(InternetPlanNotFoundException.class, () -> internetPlanService.updateInternetPlan("2", internetPlanDTO1, mockUser));

        verify(internetPlanRepo, never()).save(any());
    }

    @Test
    void deleteInternetPlan_shouldDeletePlanSuccessfully() {
        // GIVEN
        List<String> planIds = new ArrayList<>(List.of("1", "2"));
        mockUser.setInternetPlanIds(planIds);
        when(internetPlanRepo.existsById("1")).thenReturn(true);

        // WHEN
        internetPlanService.deleteInternetPlan("1", mockUser);

        // THEN
        verify(internetPlanRepo).deleteById("1");
        verify(appUserRepo).save(mockUser);
        assertFalse(mockUser.getInternetPlanIds().contains("1"));
        assertEquals(1, mockUser.getInternetPlanIds().size());
    }

    @Test
    void deleteInternetPlan_shouldThrowException_whenPlanNotFound() {
        // GIVEN
        mockUser.setInternetPlanIds(List.of("1"));
        when(internetPlanRepo.existsById("2")).thenReturn(false);

        // WHEN & THEN
        assertThrows(InternetPlanNotFoundException.class, () -> internetPlanService.deleteInternetPlan("2", mockUser));

        verify(internetPlanRepo, never()).deleteById(any());
        verify(appUserRepo, never()).save(any());
    }
}