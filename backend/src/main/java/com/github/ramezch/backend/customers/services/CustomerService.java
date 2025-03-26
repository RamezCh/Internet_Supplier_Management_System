package com.github.ramezch.backend.customers.services;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import com.github.ramezch.backend.exceptions.CustomerNotFoundException;
import com.github.ramezch.backend.exceptions.UserNotFoundException;
import com.github.ramezch.backend.exceptions.UsernameExistsException;
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

        List<String> customerIds = appUser.getCustomerIds();
        if (customerIds == null || customerIds.isEmpty()) {
            return Page.empty();
        }

        return customerRepo.findByUsernameIn(customerIds, pageable);
    }

    public Optional<Customer> getCustomer(String username) {
        return customerRepo.findByUsername(username);
    }

    public Customer addCustomer(Customer customer, String userId) {
        // 1. Find the AppUser
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. Check if username already exists among this user's customers
        List<String> customerIds = appUser.getCustomerIds();
        if (customerIds != null) {
            List<Customer> existingCustomers = customerRepo.findAllById(customerIds);
            boolean usernameExists = existingCustomers.stream()
                    .anyMatch(c -> c.username().equals(customer.username()));

            if (usernameExists) {
                throw new UsernameExistsException(customer.username());
            }
        }

        // 3. Save the new customer
        Customer savedCustomer = customerRepo.save(customer);

        // 4. Add customer ID to AppUser's customerIds list
        if (appUser.getCustomerIds() == null) {
            appUser.setCustomerIds(new ArrayList<>());
        }
        appUser.getCustomerIds().add(savedCustomer.username());
        appUserRepository.save(appUser);

        return savedCustomer;
    }

    public Customer updateCustomer(String username, Customer customer) {
        getCustomer(username).orElseThrow(() -> new CustomerNotFoundException(username));
        return customerRepo.save(customer);
    }

    public void deleteCustomer(String username, String userId) {
        // Verify customer exists
        if (customerRepo.findByUsername(username).isEmpty()) {
            throw new CustomerNotFoundException(username);
        }

        // Update user's customer list
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        appUser.getCustomerIds().removeIf(id -> id.equals(username));
        appUserRepository.save(appUser);

        // Delete customer
        customerRepo.deleteByUsername(username);
    }
}
