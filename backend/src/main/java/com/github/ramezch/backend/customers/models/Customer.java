package com.github.ramezch.backend.customers.models;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;

public record Customer(@Id @NotBlank(message = "Username can't be blank") String username, @NotBlank(message = "Full Name can't be blank") String fullName, String notes) {
    // isExpired, renewalDate, address, phone number, package, notes
}
