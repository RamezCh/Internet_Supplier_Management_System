package com.github.ramezch.backend.internetplan.services;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.exceptions.InternetPlanNameTakenException;
import com.github.ramezch.backend.exceptions.InternetPlanNotFoundException;
import com.github.ramezch.backend.internetplan.models.InternetPlan;
import com.github.ramezch.backend.internetplan.models.InternetPlanDTO;
import com.github.ramezch.backend.internetplan.models.InternetPlanSmallDTO;
import com.github.ramezch.backend.internetplan.repositories.InternetPlanRepository;
import com.github.ramezch.backend.utils.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InternetPlanService {
    private final InternetPlanRepository internetPlanRepo;
    private final AppUserRepository appUserRepository;
    private final IdService idService;

    public List<InternetPlan> getInternetPlans(AppUser appUser) {
        List<String> internetPlanIds = appUser.getInternetPlanIds() != null
                ? appUser.getInternetPlanIds()
                : List.of();

        return internetPlanIds.isEmpty()
                ? List.of()
                : internetPlanRepo.findAllById(internetPlanIds);
    }

    public List<InternetPlanSmallDTO> getActivePlansByAppUser(AppUser appUser) {
        if (appUser.getInternetPlanIds() == null || appUser.getInternetPlanIds().isEmpty()) {
            return List.of();
        }

        List<InternetPlan> activePlans = internetPlanRepo.findByIdInAndActiveTrue(appUser.getInternetPlanIds());

        return activePlans.stream()
                .map(plan -> new InternetPlanSmallDTO(plan.id(), plan.name()))
                .collect(Collectors.toList());
    }

    public Optional<InternetPlan> getInternetPlan(String id, AppUser appUser) {
        if(!appUser.getInternetPlanIds().contains(id)) {
            return Optional.empty();
        }
        return internetPlanRepo.findById(id);
    }

    public InternetPlan addInternetPlan(InternetPlanDTO internetPlanDTO, AppUser appUser) {
        List<String> internetPlanIds = Optional.ofNullable(appUser.getInternetPlanIds())
                .orElseGet(ArrayList::new);
        boolean nameExists = internetPlanRepo.existsByNameAndIdIn(internetPlanDTO.name(), internetPlanIds);
        if(nameExists) {
            throw new InternetPlanNameTakenException(internetPlanDTO.name());
        }

        String newInternetPlanID;
        do {
            newInternetPlanID = idService.randomId();
        } while (internetPlanRepo.existsById(newInternetPlanID));

        InternetPlan newInternetPlan = new InternetPlan(newInternetPlanID, internetPlanDTO.name(),
                internetPlanDTO.speed(), internetPlanDTO.price(), internetPlanDTO.bandwidth(), internetPlanDTO.isActive());

        internetPlanRepo.save(newInternetPlan);

        internetPlanIds.add(newInternetPlanID);
        appUser.setInternetPlanIds(internetPlanIds);
        appUserRepository.save(appUser);

        return newInternetPlan;
    }

    public InternetPlan updateInternetPlan(String id, InternetPlanDTO internetPlanDTO, AppUser appUser) {
        if( !appUser.getInternetPlanIds().contains(id)) {
            throw new InternetPlanNotFoundException(id);
        }

        List<String> internetPlanIds = Optional.ofNullable(appUser.getInternetPlanIds())
                .orElseGet(ArrayList::new);
        boolean nameExists = internetPlanRepo.existsByNameAndIdIn(internetPlanDTO.name(), internetPlanIds);
        if(nameExists) {
            throw new InternetPlanNameTakenException(internetPlanDTO.name());
        }

        InternetPlan updatedInternetPlan = new InternetPlan(id, internetPlanDTO.name(), internetPlanDTO.speed(), internetPlanDTO.price(), internetPlanDTO.bandwidth(), internetPlanDTO.isActive());
        return internetPlanRepo.save(updatedInternetPlan);
    }

    public void deleteInternetPlan(String id, AppUser appUser) {
        if (!internetPlanRepo.existsById(id)) {
            throw new InternetPlanNotFoundException(id);
        }

        List<String> internetPlanIds = appUser.getInternetPlanIds();

        internetPlanIds.remove(id);
        appUser.setInternetPlanIds(internetPlanIds);

        appUserRepository.save(appUser);
        internetPlanRepo.deleteById(id);
    }
    
}
