package com.github.ramezch.backend.invoice.models;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.With;

public record InvoiceUpdateDTO(
        @NotBlank String id,
        @With @PositiveOrZero double amountPaid
) {}