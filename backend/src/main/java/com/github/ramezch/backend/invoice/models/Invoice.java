package com.github.ramezch.backend.invoice.models;

import java.time.Instant;

public record Invoice(
        String id,
        String subscriptionId,
        Instant issueDate,
        Instant dueDate,
        double amountPaid,
        boolean isPaid
) {

    public boolean isPaid() {
        return isPaid;
    }
}
