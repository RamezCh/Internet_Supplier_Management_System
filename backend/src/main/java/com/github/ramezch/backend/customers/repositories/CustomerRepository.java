package com.github.ramezch.backend.customers.repositories;

import com.github.ramezch.backend.customers.models.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    @NonNull
    Page<Customer> findAll(@NonNull Pageable pageable);
    Optional<Customer> findByUsername(String username);
    void deleteByUsername(String username);
}
