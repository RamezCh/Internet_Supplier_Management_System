package com.github.ramezch.backend.customers.controllers;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.models.CustomerDTO;
import com.github.ramezch.backend.customers.models.CustomerStatus;
import com.github.ramezch.backend.customers.services.CustomerService;
import com.github.ramezch.backend.exceptions.CustomerNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    public Page<Customer> getCustomers(Pageable pageable, @AuthenticationPrincipal AppUser appUser) {
        return customerService.getCustomers(pageable, appUser);
    }

    @GetMapping("/search")
    public Page<Customer> searchCustomers(
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) String searchTerm,
            Pageable pageable,
            @AuthenticationPrincipal AppUser appUser) {
        return customerService.searchCustomers(appUser, status, searchTerm, pageable);
    }

    @GetMapping("{id}")
    public Customer getCustomer(@PathVariable String id, @AuthenticationPrincipal AppUser appUser) {
        Optional<Customer> customer = customerService.getCustomer(id, appUser);
        if(customer.isPresent()) {
            return customer.get();
        }
        throw new CustomerNotFoundException(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer addCustomer(@RequestBody @Valid CustomerDTO customerDTO, @AuthenticationPrincipal AppUser appUser, @RequestParam String internetPlanId) {
        return customerService.addCustomer(customerDTO, appUser, internetPlanId);
    }

    @PutMapping("{id}")
    public Customer updateCustomer(@PathVariable String id, @RequestBody @Valid Customer customer, @AuthenticationPrincipal AppUser appUser) {
        return customerService.updateCustomer(id, customer, appUser);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable String id, @AuthenticationPrincipal AppUser appUser) {
        customerService.deleteCustomer(id, appUser);
    }
}
