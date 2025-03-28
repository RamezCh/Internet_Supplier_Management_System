package com.github.ramezch.backend.customers.services;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.models.CustomerDTO;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import com.github.ramezch.backend.exceptions.CustomerNotFoundException;
import com.github.ramezch.backend.exceptions.UserNotFoundException;
import com.github.ramezch.backend.exceptions.UsernameTakenException;
import com.github.ramezch.backend.utils.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepo;
    private final AppUserRepository appUserRepository;
    private final IdService idService;

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

    public Customer addCustomer(CustomerDTO customerDTO, String userId) {
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Check if username already exists among this user's customers
        if (appUser.getCustomerIds() != null) {
            boolean usernameExists = customerRepo.findAllById(appUser.getCustomerIds())
                    .stream()
                    .anyMatch(c -> c.username().equals(customerDTO.username()));

            if (usernameExists) {
                throw new UsernameTakenException(customerDTO.username());
            }
        }


        String newCustomerID;
        do {
            newCustomerID = idService.randomId();
        } while (customerRepo.existsById(newCustomerID));

        LocalDate registrationDate = LocalDate.now();

        Customer newCustomer = new Customer(newCustomerID, customerDTO.username(), customerDTO.fullName(),
                customerDTO.phone(), customerDTO.address(), registrationDate, customerDTO.status(),  customerDTO.notes());

        Customer savedCustomer = customerRepo.save(newCustomer);

        // Initialize customerIds if null and add new ID
        List<String> customerIds = Optional.ofNullable(appUser.getCustomerIds())
                .orElseGet(ArrayList::new);
        customerIds.add(newCustomerID);
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