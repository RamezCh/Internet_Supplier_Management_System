package com.github.ramezch.backend.customers.services;

import com.github.ramezch.backend.customers.models.Customer;
import com.github.ramezch.backend.customers.repositories.CustomerRepository;
import com.github.ramezch.backend.exceptions.CustomerNotFoundException;
import com.github.ramezch.backend.exceptions.UsernameExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {
    private CustomerRepository repo;
    private CustomerService service;
    private Customer customer1, customer2;

    @BeforeEach
    void setup() {
        repo = mock(CustomerRepository.class);
        service = new CustomerService(repo);
        customer1 = new Customer("first_customer", "First Customer", "first to pay");
        customer2 = new Customer("second_customer", "Second Customer", "second to pay");
    }

    @Test
    void getCustomers_returnCustomers_whenFound() {
        // GIVEN
        List<Customer> customers = List.of(customer1, customer2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> expected = new PageImpl<>(customers, pageable, customers.size());
        // WHEN
        when(repo.findAll(pageable)).thenReturn(expected);
        Page<Customer> actual = service.getCustomers(pageable);
        // THEN
        verify(repo).findAll(pageable);
        assertEquals(expected, actual);
    }

    @Test
    void getCustomers_returnEmpty_whenNotFound() {
        // GIVEN
        List<Customer> customers = List.of();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> expected = new PageImpl<>(customers, pageable, 0);
        // WHEN
        when(repo.findAll(pageable)).thenReturn(expected);
        Page<Customer> actual = service.getCustomers(pageable);
        // THEN
        verify(repo).findAll(pageable);
        assertEquals(expected, actual);
    }

    @Test
    void getCustomer_returnCustomer_whenFound() {
        // GIVEN
        String username = "first_customer";
        Optional<Customer> expected = Optional.of(customer1);
        // WHEN
        when(repo.findByUsername(username)).thenReturn(expected);
        Optional<Customer> actual = service.getCustomer(username);
        // THEN
        verify(repo).findByUsername(username);
        assertEquals(expected, actual);
    }

    @Test
    void getCustomer_returnEmpty_whenNotFound() {
        // GIVEN
        String username = "third_customer";
        Optional<Customer> expected = Optional.empty();
        // WHEN
        when(repo.findByUsername(username)).thenReturn(expected);
        Optional<Customer> actual = service.getCustomer(username);
        // THEN
        verify(repo).findByUsername(username);
        assertEquals(expected, actual);
    }

    @Test
    void addCustomer_returnNewCustomer_whenUsernameNotExist() {
        // GIVEN
        when(repo.findByUsername(customer1.username())).thenReturn(Optional.empty());
        when(repo.save(customer1)).thenReturn(customer1);
        // WHEN
        Customer actual = service.addCustomer(customer1);
        // THEN
        verify(repo).findByUsername(customer1.username());
        verify(repo).save(customer1);
        assertEquals(customer1, actual);
    }

    @Test
    void addCustomer_returnException_whenUsernameExist() {
        // GIVEN
        when(repo.findByUsername(customer1.username())).thenReturn(Optional.of(customer1));
        // WHEN & THEN
        assertThrows(UsernameExistsException.class, () -> service.addCustomer(customer1));
        verify(repo).findByUsername(customer1.username());
        // never means not called
        verify(repo, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_returnNewCustomer_whenFound() {
        // GIVEN
        Customer expected = new Customer(customer1.username(), customer1.fullName(), "I like apples");
        when(repo.findByUsername(expected.username())).thenReturn(Optional.ofNullable(customer1));
        when(repo.save(expected)).thenReturn(expected);
        // WHEN
        Customer actual = service.updateCustomer(customer1.username(), expected);
        // THEN
        assertEquals(expected, actual);
        verify(repo).findByUsername(expected.username());
        verify(repo).save(expected);
    }

    @Test
    void updateCustomer_returnException_whenNotFound() {
        // GIVEN
        String username = customer1.username();
        when(repo.findByUsername(username)).thenReturn(Optional.empty());
        // WHEN & THEN
        assertThrows(CustomerNotFoundException.class, () -> service.updateCustomer(username, customer1));
        verify(repo).findByUsername(username);
        verify(repo, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_returnNothing_whenFound() {
        // GIVEN
        String username = customer1.username();
        when(repo.findByUsername(username)).thenReturn(Optional.of(customer1));
        // WHEN
        service.deleteCustomer(username);
        // THEN
        verify(repo).findByUsername(username);
        verify(repo).deleteByUsername(username);
    }

    @Test
    void deleteCustomer_returnException_whenNotFound() {
        // GIVEN
        String username = customer1.username();
        when(repo.findByUsername(username)).thenReturn(Optional.empty());
        // WHEN & THEN
        assertThrows(CustomerNotFoundException.class, () -> service.deleteCustomer(username));
        verify(repo).findByUsername(username);
        verify(repo, never()).deleteByUsername(username);
    }
}