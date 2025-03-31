package com.github.ramezch.backend.customers.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.annotation.Nullable;

public record Address(
        @NotBlank(message = "Address ID cannot be blank")
        String id,

        @Nullable
        @Size(max = 100, message = "Country must be less than 100 characters")
        String country,

        @NotBlank(message = "City cannot be blank")
        @Size(max = 100, message = "City must be less than 100 characters")
        String city,

        @NotBlank(message = "Street address cannot be blank")
        @Size(max = 200, message = "Street address must be less than 200 characters")
        String street,

        @Nullable
        @Pattern(regexp = "^[a-zA-Z0-9\\-\\s]{3,10}$",
                message = "Postal code must be 3-10 alphanumeric characters")
        String postalCode
) {}