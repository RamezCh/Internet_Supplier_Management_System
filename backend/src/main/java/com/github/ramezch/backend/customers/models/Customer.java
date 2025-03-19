package com.github.ramezch.backend.customers.models;

import org.springframework.data.annotation.Id;

public record Customer(@Id String username, String fullName, String notes) {
    // isExpired, renewalDate, address, phone number, package, notes
}
