package com.github.ramezch.backend.customers.controllers;

import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.services.CustomerService;
import com.github.ramezch.backend.exceptions.CustomerNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    public Page<Customer> getCustomers(Pageable pageable, @AuthenticationPrincipal OAuth2User appUser) {
        String userId = appUser.getName();
        return customerService.getCustomers(pageable, userId);
    }

    @GetMapping("{username}")
    public Customer getCustomer(@PathVariable String username) {
        Optional<Customer> customer = customerService.getCustomer(username);
        if(customer.isPresent()) {
            return customer.get();
        }
        throw new CustomerNotFoundException(username);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer addCustomer(@RequestBody @Valid Customer customer, @AuthenticationPrincipal OAuth2User appUser) {
        String userId = appUser.getName();
        return customerService.addCustomer(customer, userId);
    }

    @PutMapping("{username}")
    public Customer updateCustomer(@PathVariable String username, @RequestBody @Valid Customer customer) {
        return customerService.updateCustomer(username, customer);
    }

    @DeleteMapping("{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable String username, @AuthenticationPrincipal OAuth2User appUser) {
        String userId = appUser.getName();
        customerService.deleteCustomer(username, userId);
    }
}
