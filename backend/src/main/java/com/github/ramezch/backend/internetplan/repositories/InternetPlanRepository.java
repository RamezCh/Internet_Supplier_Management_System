package com.github.ramezch.backend.internetplan.repositories;

import com.github.ramezch.backend.internetplan.models.InternetPlan;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InternetPlanRepository extends MongoRepository<InternetPlan, String> {
    boolean existsByNameAndIdIn(String name, List<String> internetPlanIds);
}
