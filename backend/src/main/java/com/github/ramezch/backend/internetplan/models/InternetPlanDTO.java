package com.github.ramezch.backend.internetplan.models;

public record InternetPlanDTO(String name, String speed, double price, String bandwidth, boolean isActive) {
}
