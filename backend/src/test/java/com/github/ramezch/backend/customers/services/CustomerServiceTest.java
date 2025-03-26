package com.github.ramezch.backend.customers.services;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import com.github.ramezch.backend.exceptions.CustomerNotFoundException;
import com.github.ramezch.backend.exceptions.UserNotFoundException;
import com.github.ramezch.backend.exceptions.UsernameExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {
    private CustomerRepository customerRepo;
    private AppUserRepository appUserRepo;
    private CustomerService service;
    private Customer customer1, customer2;
    private String userId;

    @BeforeEach
    void setup() {
        customerRepo = mock(CustomerRepository.class);
        appUserRepo = mock(AppUserRepository.class);
        service = new CustomerService(customerRepo, appUserRepo);
        customer1 = new Customer("first_customer", "First Customer", "first to pay");
        customer2 = new Customer("second_customer", "Second Customer", "second to pay");
        userId = "user123";
    }

    @Test
    void getCustomers_returnCustomers_whenFound() {
        // GIVEN
        List<String> customerIds = List.of("first_customer", "second_customer");
        List<Customer> customers = List.of(customer1, customer2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> expected = new PageImpl<>(customers, pageable, customers.size());

        // Mock user repository
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(customerIds);
        when(appUserRepo.findById(userId)).thenReturn(Optional.of(mockUser));

        // Mock customer repository
        when(customerRepo.findByUsernameIn(customerIds, pageable)).thenReturn(expected);

        // WHEN
        Page<Customer> actual = service.getCustomers(pageable, userId);

        // THEN
        verify(appUserRepo).findById(userId);
        verify(customerRepo).findByUsernameIn(customerIds, pageable);
        assertEquals(expected, actual);
    }

    @Test
    void getCustomers_returnEmpty_whenNotFound() {
        // GIVEN
        List<String> customerIds = List.of();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> expected = Page.empty();

        // mock user repo
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(customerIds);
        when(appUserRepo.findById(userId)).thenReturn(Optional.of(mockUser));

        // mock customer repository
        when(customerRepo.findByUsernameIn(customerIds, pageable)).thenReturn(expected);

        // WHEN
        Page<Customer> actual = service.getCustomers(pageable, userId);
        // THEN
        verify(appUserRepo).findById(userId);
        assertEquals(expected, actual);
    }

    @Test
    void getCustomer_returnCustomer_whenFound() {
        // GIVEN
        String username = "first_customer";
        Optional<Customer> expected = Optional.of(customer1);
        // WHEN
        when(customerRepo.findByUsername(username)).thenReturn(expected);
        Optional<Customer> actual = service.getCustomer(username);
        // THEN
        verify(customerRepo).findByUsername(username);
        assertEquals(expected, actual);
    }

    @Test
    void getCustomer_returnEmpty_whenNotFound() {
        // GIVEN
        String username = "third_customer";
        Optional<Customer> expected = Optional.empty();
        // WHEN
        when(customerRepo.findByUsername(username)).thenReturn(expected);
        Optional<Customer> actual = service.getCustomer(username);
        // THEN
        verify(customerRepo).findByUsername(username);
        assertEquals(expected, actual);
    }

    @Test
    void addCustomer_shouldSaveNewCustomerAndUpdateUser_whenUsernameNotExist() {
        // GIVEN
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(new ArrayList<>());

        when(appUserRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(customerRepo.findAllById(anyList())).thenReturn(List.of());
        when(customerRepo.save(customer1)).thenReturn(customer1);
        when(appUserRepo.save(any(AppUser.class))).thenReturn(mockUser);

        // WHEN
        Customer actual = service.addCustomer(customer1, userId);

        // THEN
        verify(appUserRepo).findById(userId);
        verify(customerRepo).findAllById(mockUser.getCustomerIds());
        verify(customerRepo).save(customer1);
        verify(appUserRepo).save(mockUser);

        assertEquals(customer1, actual);
        assertTrue(mockUser.getCustomerIds().contains(customer1.username()));
    }

    @Test
    void addCustomer_shouldThrowException_whenUserNotFound() {
        // GIVEN
        when(appUserRepo.findById(userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(UserNotFoundException.class, () -> service.addCustomer(customer1, userId));
        verify(appUserRepo).findById(userId);
        verifyNoInteractions(customerRepo);
    }

    @Test
    void addCustomer_shouldThrowException_whenUsernameExists() {
        // GIVEN
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(List.of("existing_customer"));

        Customer existingCustomer = new Customer("existing_customer", "Existing", "customer");
        when(appUserRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(customerRepo.findAllById(mockUser.getCustomerIds())).thenReturn(List.of(existingCustomer));

        // WHEN & THEN
        Executable addCustomer = () -> service.addCustomer(
                new Customer("existing_customer", "New Name", "New Desc"),
                userId
        );
        assertThrows(UsernameExistsException.class, addCustomer);

        verify(appUserRepo).findById(userId);
        verify(customerRepo).findAllById(mockUser.getCustomerIds());
        verify(customerRepo, never()).save(any());
        verify(appUserRepo, never()).save(any());
    }

    @Test
    void addCustomer_shouldInitializeCustomerIdsList_whenNull() {
        // GIVEN
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(null); // null list

        when(appUserRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(customerRepo.findAllById(anyList())).thenReturn(List.of());
        when(customerRepo.save(customer1)).thenReturn(customer1);
        when(appUserRepo.save(any(AppUser.class))).thenReturn(mockUser);

        // WHEN
        service.addCustomer(customer1, userId);

        // THEN
        assertNotNull(mockUser.getCustomerIds());
        assertTrue(mockUser.getCustomerIds().contains(customer1.username()));
    }

    @Test
    void updateCustomer_returnNewCustomer_whenFound() {
        // GIVEN
        Customer expected = new Customer(customer1.username(), customer1.fullName(), "I like apples");
        when(customerRepo.findByUsername(expected.username())).thenReturn(Optional.ofNullable(customer1));
        when(customerRepo.save(expected)).thenReturn(expected);
        // WHEN
        Customer actual = service.updateCustomer(customer1.username(), expected);
        // THEN
        assertEquals(expected, actual);
        verify(customerRepo).findByUsername(expected.username());
        verify(customerRepo).save(expected);
    }

    @Test
    void updateCustomer_returnException_whenNotFound() {
        // GIVEN
        String username = customer1.username();
        when(customerRepo.findByUsername(username)).thenReturn(Optional.empty());
        // WHEN & THEN
        assertThrows(CustomerNotFoundException.class, () -> service.updateCustomer(username, customer1));
        verify(customerRepo).findByUsername(username);
        verify(customerRepo, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_returnNothing_whenFound() {
        // GIVEN
        String username = customer1.username();
        List<String> customerIds = new ArrayList<>(List.of(username)); // mutable list

        // Mock user repository
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(customerIds);
        when(appUserRepo.findById(userId)).thenReturn(Optional.of(mockUser));

        when(customerRepo.findByUsername(username)).thenReturn(Optional.of(customer1));

        // WHEN
        service.deleteCustomer(username, userId);

        // THEN
        verify(customerRepo).findByUsername(username);
        verify(appUserRepo).findById(userId);
        verify(customerRepo).deleteByUsername(username);

        // Verify user's customer list was updated
        assertThat(mockUser.getCustomerIds()).doesNotContain(username);
        verify(appUserRepo).save(mockUser);
    }

    @Test
    void deleteCustomer_returnException_whenNotFound() {
        // GIVEN
        String username = customer1.username();
        when(customerRepo.findByUsername(username)).thenReturn(Optional.empty());

        // Mock user repository
        AppUser mockUser = new AppUser();
        when(appUserRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        // WHEN & THEN
        assertThrows(CustomerNotFoundException.class, () -> service.deleteCustomer(username, userId));
        verify(customerRepo).findByUsername(username);
        verify(customerRepo, never()).deleteByUsername(username);
    }
}