package com.github.ramezch.backend.internetplan.models;

import org.springframework.data.annotation.Id;

public record InternetPlan(@Id String id, String name, String speed, double price, String bandwidth, boolean isActive) {

}
