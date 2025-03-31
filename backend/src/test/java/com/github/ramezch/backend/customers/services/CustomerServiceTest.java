package com.github.ramezch.backend.customers.services;

import com.github.ramezch.backend.customers.models.Address;
import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.models.CustomerDTO;
import com.github.ramezch.backend.customers.models.CustomerStatus;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import com.github.ramezch.backend.exceptions.CustomerNotFoundException;
import com.github.ramezch.backend.exceptions.UserNotFoundException;
import com.github.ramezch.backend.exceptions.UsernameTakenException;
import com.github.ramezch.backend.utils.IdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {
    private CustomerRepository customerRepo;
    private AppUserRepository appUserRepo;
    private IdService idService;
    private CustomerService service;
    private Customer customer1, customer2;
    private CustomerDTO customerDTO1;
    private String userId;

    @BeforeEach
    void setup() {
        customerRepo = mock(CustomerRepository.class);
        appUserRepo = mock(AppUserRepository.class);
        idService = mock(IdService.class);
        LocalDate now = LocalDate.now();
        Address address = new Address(idService.randomId(),"Deutschland", "Berlin", "BeispielStrasse", "10000");
        service = new CustomerService(customerRepo, appUserRepo, idService);
        customer1 = new Customer("123","new_customer", "New Customer", "78863120", address, now, CustomerStatus.PENDING_ACTIVATION, "test");
        customer2 = new Customer("234","new_customer2", "New Customer 2", "78863121", address, now, CustomerStatus.PENDING_ACTIVATION, "test2");
        customerDTO1 = new CustomerDTO("new_customer", "New Customer", "78863120", address, CustomerStatus.PENDING_ACTIVATION, "test");
        userId = "user123";
    }

    @Test
    void getCustomers_returnCustomers_whenFound() {
        // GIVEN
        List<String> customerIds = List.of("123", "234");
        List<Customer> customers = List.of(customer1, customer2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> expected = new PageImpl<>(customers, pageable, customers.size());

        // Mock user repository
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(customerIds);
        when(appUserRepo.findById(userId)).thenReturn(Optional.of(mockUser));

        // Mock customer repository
        when(customerRepo.findByIdIn(customerIds, pageable)).thenReturn(expected);

        // WHEN
        Page<Customer> actual = service.getCustomers(pageable, userId);

        // THEN
        verify(appUserRepo).findById(userId);
        verify(customerRepo).findByIdIn(customerIds, pageable);
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
        when(customerRepo.findByIdIn(customerIds, pageable)).thenReturn(expected);

        // WHEN
        Page<Customer> actual = service.getCustomers(pageable, userId);
        // THEN
        verify(appUserRepo).findById(userId);
        assertEquals(expected, actual);
    }

    @Test
    void getCustomer_returnCustomer_whenFound() {
        // GIVEN
        String id = "123";
        Optional<Customer> expected = Optional.of(customer1);
        // WHEN
        when(customerRepo.findById(id)).thenReturn(expected);
        Optional<Customer> actual = service.getCustomer(id);
        // THEN
        verify(customerRepo).findById(id);
        assertEquals(expected, actual);
    }

    @Test
    void getCustomer_returnEmpty_whenNotFound() {
        // GIVEN
        String id = "999";
        Optional<Customer> expected = Optional.empty();
        // WHEN
        when(customerRepo.findById(id)).thenReturn(expected);
        Optional<Customer> actual = service.getCustomer(id);
        // THEN
        verify(customerRepo).findById(id);
        assertEquals(expected, actual);
    }

    @Test
    void addCustomer_shouldSaveNewCustomerAndUpdateUser_whenUsernameNotExist() {
        // GIVEN
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(new ArrayList<>());

        when(appUserRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(customerRepo.findAllById(anyList())).thenReturn(List.of());
        when(idService.randomId()).thenReturn("123");
        when(customerRepo.save(any(Customer.class))).thenReturn(customer1);
        when(appUserRepo.save(any(AppUser.class))).thenReturn(mockUser);

        // WHEN
        Customer actual = service.addCustomer(customerDTO1, userId);

        // THEN
        verify(appUserRepo).findById(userId);
        verify(customerRepo).findAllById(mockUser.getCustomerIds());
        verify(customerRepo).save(any(Customer.class));
        verify(appUserRepo).save(mockUser);

        assertEquals(customer1.username(), actual.username());
        assertTrue(mockUser.getCustomerIds().contains(actual.id()));
    }

    @Test
    void addCustomer_shouldThrowException_whenUserNotFound() {
        // GIVEN
        when(appUserRepo.findById(userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(UserNotFoundException.class, () -> service.addCustomer(customerDTO1, userId));
        verify(appUserRepo).findById(userId);
        verifyNoInteractions(customerRepo);
    }

    @Test
    void addCustomer_shouldThrowException_whenUsernameExists() {
        // GIVEN
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(List.of("123"));

        when(appUserRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(customerRepo.findAllById(mockUser.getCustomerIds())).thenReturn(List.of(customer1));

        // WHEN & THEN
        Executable addCustomer = () -> service.addCustomer(customerDTO1, userId);
        assertThrows(UsernameTakenException.class, addCustomer);

        verify(appUserRepo).findById(userId);
        verify(customerRepo).findAllById(mockUser.getCustomerIds());
        verify(customerRepo, never()).save(any());
        verify(appUserRepo, never()).save(any());
    }

    @Test
    void addCustomer_shouldInitializeCustomerIdsList_whenNull() {
        // GIVEN
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(null);

        when(appUserRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(customerRepo.findAllById(anyList())).thenReturn(List.of());
        when(idService.randomId()).thenReturn("123");
        when(customerRepo.save(any(Customer.class))).thenReturn(customer1);
        when(appUserRepo.save(any(AppUser.class))).thenReturn(mockUser);

        // WHEN
        Customer actual = service.addCustomer(customerDTO1, userId);

        // THEN
        assertNotNull(mockUser.getCustomerIds());
        assertTrue(mockUser.getCustomerIds().contains(actual.id()));
    }

    @Test
    void updateCustomer_returnNewCustomer_whenFound() {
        // GIVEN
        Customer updatedCustomer = new Customer("123", "updated_customer", "Updated Customer", "78863120",
                customer1.address(), customer1.registrationDate(), CustomerStatus.ACTIVE, "updated notes");

        when(customerRepo.findById("123")).thenReturn(Optional.of(customer1));
        when(customerRepo.save(updatedCustomer)).thenReturn(updatedCustomer);

        // WHEN
        Customer actual = service.updateCustomer("123", updatedCustomer);

        // THEN
        assertEquals(updatedCustomer, actual);
        verify(customerRepo).findById("123");
        verify(customerRepo).save(updatedCustomer);
    }

    @Test
    void updateCustomer_returnException_whenNotFound() {
        // GIVEN
        String id = "123";
        when(customerRepo.findById(id)).thenReturn(Optional.empty());
        // WHEN & THEN
        assertThrows(CustomerNotFoundException.class, () -> service.updateCustomer(id, customer1));
        verify(customerRepo).findById(id);
        verify(customerRepo, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_returnNothing_whenFound() {
        // GIVEN
        String id = "123";
        List<String> customerIds = new ArrayList<>(List.of(id)); // mutable list

        // Mock user repository
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(customerIds);
        when(appUserRepo.findById(userId)).thenReturn(Optional.of(mockUser));

        when(customerRepo.existsById(id)).thenReturn(true);

        // WHEN
        service.deleteCustomer(id, userId);

        // THEN
        verify(customerRepo).existsById(id);
        verify(appUserRepo).findById(userId);

        // Verify user's customer list was updated
        assertThat(mockUser.getCustomerIds()).doesNotContain(id);
        verify(appUserRepo).save(mockUser);
        verify(customerRepo).deleteById(id);
    }

    @Test
    void deleteCustomer_returnException_whenNotFound() {
        // GIVEN
        String id = "123";
        when(customerRepo.existsById(id)).thenReturn(false);

        // Mock user repository
        AppUser mockUser = new AppUser();
        when(appUserRepo.findById(userId)).thenReturn(Optional.of(mockUser));

        // WHEN & THEN
        assertThrows(CustomerNotFoundException.class, () -> service.deleteCustomer(id, userId));
        verify(customerRepo).existsById(id);
        verify(customerRepo, never()).deleteById(any());
    }
}