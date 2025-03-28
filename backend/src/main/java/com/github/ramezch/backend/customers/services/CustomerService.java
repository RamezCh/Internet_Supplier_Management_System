package com.github.ramezch.backend.customers.services;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import com.github.ramezch.backend.exceptions.CustomerNotFoundException;
import com.github.ramezch.backend.exceptions.IdTakenException;
import com.github.ramezch.backend.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepo;
    private final AppUserRepository appUserRepository;

    public Page<Customer> getCustomers(Pageable pageable, String userId) {
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<String> customerIds = Optional.ofNullable(appUser.getCustomerIds())
                .orElseGet(ArrayList::new);

        return customerIds.isEmpty()
                ? Page.empty()
                : customerRepo.findByIdIn(customerIds, pageable);
    }

    public Optional<Customer> getCustomer(String id) {
        return customerRepo.findById(id);
    }

    public Customer addCustomer(Customer customer, String userId) {
        if (customerRepo.existsById(customer.id())) {
            throw new IdTakenException(customer.id());
        }

        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Customer savedCustomer = customerRepo.save(customer);

        // Initialize customerIds if null and add new ID
        List<String> customerIds = Optional.ofNullable(appUser.getCustomerIds())
                .orElseGet(ArrayList::new);
        customerIds.add(savedCustomer.id());
        appUser.setCustomerIds(customerIds);
        appUserRepository.save(appUser);

        return savedCustomer;
    }

    public Customer updateCustomer(String id, Customer updatedCustomer) {
        customerRepo.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        return customerRepo.save(updatedCustomer);
    }

    public void deleteCustomer(String id, String userId) {
        if (!customerRepo.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }

        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Optional.ofNullable(appUser.getCustomerIds())
                .ifPresent(ids -> ids.remove(id));
        appUserRepository.save(appUser);

        customerRepo.deleteById(id);
    }
}