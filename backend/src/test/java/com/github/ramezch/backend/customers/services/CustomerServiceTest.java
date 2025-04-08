package com.github.ramezch.backend.customers.services;

import com.github.ramezch.backend.customers.models.Address;
import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.customers.models.*;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import com.github.ramezch.backend.exceptions.*;
import com.github.ramezch.backend.utils.IdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {
    private CustomerRepository customerRepo;
    private AppUserRepository appUserRepo;
    private IdService idService;
    private CustomerService service;
    private Customer customer1, customer2;
    private CustomerDTO customerDTO1;
    private AppUser mockUser;
    private final Address address = new Address("addr1", "Deutschland", "Berlin", "BeispielStrasse", "10000");

    @BeforeEach
    void setup() {
        customerRepo = mock(CustomerRepository.class);
        appUserRepo = mock(AppUserRepository.class);
        idService = mock(IdService.class);
        service = new CustomerService(customerRepo, appUserRepo, idService);

        Instant now = Instant.now();
        customer1 = new Customer("123", "new_customer", "New Customer", "78863120", address, now, CustomerStatus.PENDING_ACTIVATION, "test");
        customer2 = new Customer("234", "new_customer2", "New Customer 2", "78863121", address, now, CustomerStatus.PENDING_ACTIVATION, "test2");
        customerDTO1 = new CustomerDTO("new_customer", "New Customer", "78863120", address, CustomerStatus.PENDING_ACTIVATION, "test");

        mockUser = new AppUser();
        String userId = "user123";
        mockUser.setId(userId);
        when(appUserRepo.findById(userId)).thenReturn(Optional.of(mockUser));
    }

    @Test
    void getCustomers_returnCustomers_whenFound() {
        List<String> customerIds = List.of("123", "234");
        mockUser.setCustomerIds(customerIds);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> expected = new PageImpl<>(List.of(customer1, customer2), pageable, 2);

        when(customerRepo.findByIdIn(customerIds, pageable)).thenReturn(expected);

        Page<Customer> actual = service.getCustomers(pageable, mockUser);

        verify(customerRepo).findByIdIn(customerIds, pageable);
        assertEquals(expected, actual);
    }

    @Test
    void getCustomers_returnEmpty_whenNotFound() {
        mockUser.setCustomerIds(List.of());
        Pageable pageable = PageRequest.of(0, 10);

        when(customerRepo.findByIdIn(anyList(), eq(pageable))).thenReturn(Page.empty());

        Page<Customer> actual = service.getCustomers(pageable, mockUser);
        assertTrue(actual.isEmpty());
    }

    @Test
    void getCustomer_returnCustomer_whenFound() {
        String id = "123";
        mockUser.setCustomerIds(List.of(id));

        when(customerRepo.findById(id)).thenReturn(Optional.of(customer1));

        Optional<Customer> actual = service.getCustomer(id, mockUser);
        assertTrue(actual.isPresent());
        assertEquals(customer1, actual.get());
    }

    @Test
    void getCustomer_returnEmpty_whenNotFound() {
        mockUser.setCustomerIds(List.of("1"));
        Optional<Customer> actual = service.getCustomer("999", mockUser);
        assertTrue(actual.isEmpty());
    }

    @Test
    void addCustomer_shouldSaveNewCustomerAndUpdateUser_whenUsernameNotExist() {
        mockUser.setCustomerIds(new ArrayList<>());
        when(idService.randomId()).thenReturn("123");
        when(customerRepo.save(any())).thenReturn(customer1);

        Customer actual = service.addCustomer(customerDTO1, mockUser);

        verify(customerRepo).save(any());
        verify(appUserRepo).save(mockUser);
        assertEquals(customer1.username(), actual.username());
        assertTrue(mockUser.getCustomerIds().contains(actual.id()));
    }

    @Test
    void addCustomer_shouldThrowException_whenUsernameExists() {
        mockUser.setCustomerIds(List.of("123"));
        when(customerRepo.existsByUsernameAndIdIn(anyString(), anyList())).thenReturn(true);

        assertThrows(UsernameTakenException.class, () -> service.addCustomer(customerDTO1, mockUser));
        verify(customerRepo, never()).save(any());
    }

    @Test
    void addCustomer_shouldInitializeCustomerIdsList_whenNull() {
        mockUser.setCustomerIds(null);
        when(idService.randomId()).thenReturn("123");
        when(customerRepo.save(any())).thenReturn(customer1);

        Customer actual = service.addCustomer(customerDTO1, mockUser);

        assertNotNull(mockUser.getCustomerIds());
        assertTrue(mockUser.getCustomerIds().contains(actual.id()));
    }

    @Test
    void updateCustomer_returnNewCustomer_whenFound() {
        mockUser.setCustomerIds(List.of("123"));
        Customer updatedCustomer = new Customer("123", "updated_customer", "Updated Customer", "78863120",
                customer1.address(), customer1.registrationDate(), CustomerStatus.ACTIVE, "updated notes");

        when(customerRepo.save(updatedCustomer)).thenReturn(updatedCustomer);

        Customer actual = service.updateCustomer("123", updatedCustomer, mockUser);
        assertEquals(updatedCustomer, actual);
    }

    @Test
    void updateCustomer_returnException_whenNotFound() {
        mockUser.setCustomerIds(List.of("1"));
        assertThrows(CustomerNotFoundException.class, () ->
                service.updateCustomer("123", customer1, mockUser));
    }

    @Test
    void deleteCustomer_returnNothing_whenFound() {
        String id = "123";
        mockUser.setCustomerIds(new ArrayList<>(List.of(id)));
        when(customerRepo.existsById(id)).thenReturn(true);

        service.deleteCustomer(id, mockUser);

        assertFalse(mockUser.getCustomerIds().contains(id));
        verify(customerRepo).deleteById(id);
    }

    @Test
    void deleteCustomer_returnException_whenNotFound() {
        when(customerRepo.existsById("123")).thenReturn(false);
        assertThrows(CustomerNotFoundException.class, () -> service.deleteCustomer("123", mockUser));
    }

    @Test
    void searchCustomers_returnFilteredCustomers_whenStatusProvided() {
        // Given
        List<String> customerIds = List.of("123", "234");
        mockUser.setCustomerIds(customerIds);
        Pageable pageable = PageRequest.of(0, 10);
        CustomerStatus status = CustomerStatus.PENDING_ACTIVATION;
        Page<Customer> expected = new PageImpl<>(List.of(customer1, customer2), pageable, 2);

        // Mock the correct repository method that will be called
        when(customerRepo.findByIdInAndStatus(customerIds, status, pageable))
                .thenReturn(expected);

        // When
        Page<Customer> actual = service.searchCustomers(mockUser, status, null, pageable);

        // Then
        verify(customerRepo).findByIdInAndStatus(customerIds, status, pageable);
        verify(customerRepo, never()).searchCustomers(any(), any(), any(), any());
        assertEquals(expected, actual);
    }

    @Test
    void searchCustomers_returnFilteredCustomers_whenSearchTermProvided() {
        // Given
        List<String> customerIds = List.of("123", "234");
        mockUser.setCustomerIds(customerIds);
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "customer";
        Page<Customer> expected = new PageImpl<>(List.of(customer1, customer2), pageable, 2);

        when(customerRepo.searchCustomers(customerIds, null, searchTerm, pageable))
                .thenReturn(expected);

        // When
        Page<Customer> actual = service.searchCustomers(mockUser, null, searchTerm, pageable);

        // Then
        verify(customerRepo).searchCustomers(customerIds, null, searchTerm, pageable);
        assertEquals(expected, actual);
    }

    @Test
    void searchCustomers_returnFilteredCustomers_whenBothStatusAndSearchTermProvided() {
        // Given
        List<String> customerIds = List.of("123", "234");
        mockUser.setCustomerIds(customerIds);
        Pageable pageable = PageRequest.of(0, 10);
        CustomerStatus status = CustomerStatus.PENDING_ACTIVATION;
        String searchTerm = "customer";
        Page<Customer> expected = new PageImpl<>(List.of(customer1, customer2), pageable, 2);

        when(customerRepo.searchCustomers(customerIds, status, searchTerm, pageable))
                .thenReturn(expected);

        // When
        Page<Customer> actual = service.searchCustomers(mockUser, status, searchTerm, pageable);

        // Then
        verify(customerRepo).searchCustomers(customerIds, status, searchTerm, pageable);
        assertEquals(expected, actual);
    }

    @Test
    void searchCustomers_returnEmpty_whenNoMatchesFound() {
        // Given
        List<String> customerIds = List.of("123", "234");
        mockUser.setCustomerIds(customerIds);
        Pageable pageable = PageRequest.of(0, 10);
        CustomerStatus status = CustomerStatus.ACTIVE;
        String searchTerm = "nonexistent";

        when(customerRepo.searchCustomers(customerIds, status, searchTerm, pageable))
                .thenReturn(Page.empty());

        // When
        Page<Customer> actual = service.searchCustomers(mockUser, status, searchTerm, pageable);

        // Then
        assertTrue(actual.isEmpty());
    }

    @Test
    void searchCustomers_returnEmpty_whenUserHasNoCustomers() {
        // Given
        mockUser.setCustomerIds(List.of());
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Customer> actual = service.searchCustomers(mockUser, null, "any", pageable);

        // Then
        assertTrue(actual.isEmpty());
        verify(customerRepo, never()).searchCustomers(any(), any(), any(), any());
    }

    @Test
    void searchCustomers_usesCaseInsensitiveSearch() {
        // Given
        List<String> customerIds = List.of("123", "234");
        mockUser.setCustomerIds(customerIds);
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "CUSTOMER"; // uppercase
        Page<Customer> expected = new PageImpl<>(List.of(customer1, customer2), pageable, 2);

        when(customerRepo.searchCustomers(customerIds, null, searchTerm, pageable))
                .thenReturn(expected);

        // When
        Page<Customer> actual = service.searchCustomers(mockUser, null, searchTerm, pageable);

        // Then
        verify(customerRepo).searchCustomers(customerIds, null, searchTerm, pageable);
        assertEquals(expected, actual);
    }
}