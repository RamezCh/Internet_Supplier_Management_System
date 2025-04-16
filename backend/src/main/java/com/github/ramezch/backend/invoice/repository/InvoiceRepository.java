package com.github.ramezch.backend.invoice.repository;

import com.github.ramezch.backend.invoice.models.Invoice;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    Invoice findBySubscriptionIdAndDueDate(@NotBlank String subscriptionId, @NotBlank Instant dueDate);

    List<Invoice> findBySubscriptionId(String subscriptionId);
}
