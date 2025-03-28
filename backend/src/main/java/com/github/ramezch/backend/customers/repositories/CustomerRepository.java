package com.github.ramezch.backend.customers.repositories;

import com.github.ramezch.backend.customers.models.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    @NonNull
    Page<Customer> findByIdIn(List<String> ids, Pageable pageable);
}
