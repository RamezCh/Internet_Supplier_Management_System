package com.github.ramezch.backend.internetplan.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record InternetPlanDTO(
        String name,

        @NotBlank(message = "Speed cannot be blank")
        @Size(max = 10, message = "Speed must be at most 10 characters")
        String speed,

        @PositiveOrZero(message = "Price must be non-negative")
        double price,

        @NotBlank(message = "Bandwidth cannot be blank")
        String bandwidth,

        boolean isActive
) {}