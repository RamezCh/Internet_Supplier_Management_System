package com.github.ramezch.backend.customers.models;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;


public record Customer(
        @Id
        @NotBlank(message = "ID cannot be blank")
        String id,

        @NotBlank(message = "Username cannot be blank")
        String username,

        @NotBlank(message = "Full name cannot be blank")
        String fullName,

        @Pattern(regexp = "^\\+?[0-9\\s\\-]{6,20}$",
                message = "Phone number must be 6-20 digits with optional + prefix")
        String phone,

        @NotNull(message = "Address must be provided")
        Address address,

        @CreatedDate
        @PastOrPresent(message = "Registration date cannot be in the future")
        Instant registrationDate,

        @NotNull(message = "Status must be specified")
        CustomerStatus status,

        @Nullable
        @Size(max = 500, message = "Notes must be less than 500 characters")
        String notes
) { }