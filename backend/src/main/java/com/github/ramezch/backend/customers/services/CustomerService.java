package com.github.ramezch.backend.customers.services;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.models.CustomerDTO;
import com.github.ramezch.backend.customers.models.CustomerStatus;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import com.github.ramezch.backend.exceptions.CustomerNotFoundException;
import com.github.ramezch.backend.exceptions.UsernameTakenException;
import com.github.ramezch.backend.subscription.services.SubscriptionService;
import com.github.ramezch.backend.utils.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepo;
    private final AppUserRepository appUserRepository;
    private final IdService idService;
    private final SubscriptionService subscriptionService;

    public Page<Customer> getCustomers(Pageable pageable, AppUser appUser) {
        List<String> customerIds = Optional.ofNullable(appUser.getCustomerIds())
                .orElseGet(ArrayList::new);

        return customerIds.isEmpty()
                ? Page.empty()
                : customerRepo.findByIdIn(customerIds, pageable);
    }

    public Page<Customer> searchCustomers(AppUser appUser, CustomerStatus status, String searchTerm, Pageable pageable) {
        List<String> customerIds = appUser.getCustomerIds();
        if (customerIds == null || customerIds.isEmpty()) {
            return Page.empty();
        }

        // Case 1: No filters - return all customers for these IDs
        if (status == null && (searchTerm == null || searchTerm.isEmpty())) {
            return customerRepo.findByIdIn(customerIds, pageable);
        }

        // Case 2: Only status filter
        if (status != null && (searchTerm == null || searchTerm.isEmpty())) {
            return customerRepo.findByIdInAndStatus(customerIds, status, pageable);
        }

        // Case 3: Full search with both status and search term
        return customerRepo.searchCustomers(
                customerIds,
                status,
                searchTerm,
                pageable
        );
    }

    public Optional<Customer> getCustomer(String id, AppUser appUser) {
        if (!appUser.getCustomerIds().contains(id)) {
            return Optional.empty();
        }
        return customerRepo.findById(id);
    }

    public Customer addCustomer(CustomerDTO customerDTO, AppUser appUser, String internetPlanId) {
        List<String> customerIds = Optional.ofNullable(appUser.getCustomerIds())
                .orElseGet(ArrayList::new);

        // Check if username already exists among this user's customers
        boolean usernameExists = customerRepo.existsByUsernameAndIdIn(
                customerDTO.username(),
                customerIds
        );

        if (usernameExists) {
            throw new UsernameTakenException(customerDTO.username());
        }

        String newCustomerID;
        do {
            newCustomerID = idService.randomId();
        } while (customerRepo.existsById(newCustomerID));

        Instant registrationDate = Instant.now();

        Customer newCustomer = new Customer(newCustomerID, customerDTO.username(), customerDTO.fullName(),
                customerDTO.phone(), customerDTO.address(), registrationDate, customerDTO.status(), customerDTO.notes());

        Customer savedCustomer = customerRepo.save(newCustomer);

        subscriptionService.createSubscription(newCustomerID, internetPlanId);

        customerIds.add(newCustomerID);
        appUser.setCustomerIds(customerIds);
        appUserRepository.save(appUser);

        return savedCustomer;
    }

    public Customer updateCustomer(String id, Customer updatedCustomer, AppUser appUser) {
        if (!appUser.getCustomerIds().contains(id)) {
            throw new CustomerNotFoundException(id);
        }
        return customerRepo.save(updatedCustomer);
    }

    public void deleteCustomer(String id, AppUser appUser) {
        if (!customerRepo.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }

       List<String> customerIds = appUser.getCustomerIds();

        customerIds.remove(id);
        appUser.setCustomerIds(customerIds);

        subscriptionService.deleteSubscription(id);

        appUserRepository.save(appUser);

        customerRepo.deleteById(id);
    }

}