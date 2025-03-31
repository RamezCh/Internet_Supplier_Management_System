package com.github.ramezch.backend.customers.repositories;

import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.models.CustomerStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    @NonNull
    Page<Customer> findByIdIn(List<String> ids, Pageable pageable);

    @Query("""
    {
        $and: [
            { _id: { $in: ?0 } },
            {
                $or: [
                    { $expr: { $eq: [?1, null] } },
                    { status: ?1 }
                ]
            },
            {
                $or: [
                    { username: { $regex: ?2, $options: 'i' } },
                    { fullName: { $regex: ?2, $options: 'i' } },
                    { phone: { $regex: ?2, $options: 'i' } },
                    { 'address.city': { $regex: ?2, $options: 'i' } }
                ]
            }
        ]
    }
    """)
    Page<Customer> searchCustomers(
            List<String> customerIds,
            @Nullable CustomerStatus status,
            @Nullable String searchTerm,
            Pageable pageable);

    boolean existsByUsernameAndIdIn(@NotBlank(message = "Username cannot be blank") String username, List<String> customerIds);

    Page<Customer> findByIdInAndStatus(List<String> customerIds, CustomerStatus status, Pageable pageable);
}
