package com.github.ramezch.backend.internetplan.controllers;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.exceptions.InternetPlanNotFoundException;
import com.github.ramezch.backend.internetplan.models.InternetPlan;
import com.github.ramezch.backend.internetplan.models.InternetPlanDTO;
import com.github.ramezch.backend.internetplan.services.InternetPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internet_plans")
public class InternetPlanController {
    private final InternetPlanService internetPlanService;

    @GetMapping
    public List<InternetPlan> getInternetPlans(@AuthenticationPrincipal AppUser appUser) {
        return internetPlanService.getInternetPlans(appUser);
    }

    @GetMapping("{id}")
    public InternetPlan getInternetPlan(@AuthenticationPrincipal AppUser appUser, @PathVariable String id) {
        Optional<InternetPlan> internetPlan = internetPlanService.getInternetPlan(id, appUser);
        if(internetPlan.isPresent()) {
            return internetPlan.get();
        }
        throw new InternetPlanNotFoundException(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InternetPlan addInternetPlan(@AuthenticationPrincipal AppUser appUser, @RequestBody @Valid InternetPlanDTO internetPlanDTO) {
        return internetPlanService.addInternetPlan(internetPlanDTO, appUser);
    }

    @PutMapping("{id}")
    public InternetPlan updateInternetPlan(@AuthenticationPrincipal AppUser appUser, @PathVariable String id, @RequestBody @Valid InternetPlanDTO internetPlanDTO) {
        return internetPlanService.updateInternetPlan(id, internetPlanDTO, appUser);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInternetPlan(@AuthenticationPrincipal AppUser appUser, @PathVariable String id) {
        internetPlanService.deleteInternetPlan(id, appUser);
    }
}
