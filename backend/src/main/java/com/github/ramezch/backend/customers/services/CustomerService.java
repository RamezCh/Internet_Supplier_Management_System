package com.github.ramezch.backend.customers.services;

import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import com.github.ramezch.backend.exceptions.CustomerNotFoundException;
import com.github.ramezch.backend.exceptions.UsernameExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepo;

    public Page<Customer> getCustomers(Pageable pageable) {
        return customerRepo.findAll(pageable);
    }

    public Optional<Customer> getCustomer(String username) {
        return customerRepo.findByUsername(username);
    }

    public Customer addCustomer(Customer customer) {
        Optional<Customer> checkCustomer = customerRepo.findByUsername(customer.username());
        if(checkCustomer.isPresent()) {
            throw new UsernameExistsException(customer.username());
        }
        return customerRepo.save(customer);
    }

    public Customer updateCustomer(String username, Customer customer) {
        getCustomer(username).orElseThrow(() -> new CustomerNotFoundException(username));
        return customerRepo.save(customer);
    }

    public void deleteCustomer(String username) {
        getCustomer(username).orElseThrow(() -> new CustomerNotFoundException(username));
        customerRepo.deleteByUsername(username);
    }
}
