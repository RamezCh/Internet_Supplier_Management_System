package com.github.ramezch.backend.invoice.services;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.exceptions.InvoiceNotFoundException;
import com.github.ramezch.backend.invoice.models.Invoice;
import com.github.ramezch.backend.invoice.models.InvoiceDTO;
import com.github.ramezch.backend.invoice.models.InvoiceUpdateDTO;
import com.github.ramezch.backend.invoice.repository.InvoiceRepository;
import com.github.ramezch.backend.utils.IdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepo;
    @Mock
    private IdService idService;
    @Mock
    private AppUserRepository appUserRepo;

    @InjectMocks
    private InvoiceService invoiceService;

    private Invoice testInvoice;
    private final String testId = "test123";
    private final String custId = "cust123";
    private final String testSubscriptionId = "sub123";
    private final Instant testDueDate = Instant.now().plusSeconds(86400);
    private final Instant testIssueDate = Instant.now();
    private final double testAmountDue = 100.0;

    @BeforeEach
    void setUp() {
        testInvoice = new Invoice(testId, custId, testSubscriptionId, testIssueDate, testDueDate, testAmountDue, 0, false);
    }

    @Test
    void getInvoicesByCustomerId_shouldReturnInvoices_whenCustomerIdMatches() throws Exception {
        // GIVEN
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(List.of(custId));
        when(invoiceRepo.findAllByCustomerId(custId)).thenReturn(List.of(testInvoice));

        // WHEN
        List<Invoice> result = invoiceService.getInvoicesByCustomerId(custId, mockUser);

        // THEN
        assertEquals(1, result.size());
        assertEquals(testInvoice, result.getFirst());
        verify(invoiceRepo).findAllByCustomerId(custId);
    }

    @Test
    void getInvoicesByCustomerId_shouldThrow_whenCustomerIdNotInUser() {
        // GIVEN
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(List.of("other-cust"));

        // WHEN & THEN
        assertThrows(IllegalAccessException.class,
                () -> invoiceService.getInvoicesByCustomerId(custId, mockUser));
        verify(invoiceRepo, never()).findAllByCustomerId(any());
    }

    @Test
    void getInvoiceById_shouldReturnInvoice_whenFoundAndAuthorized() throws Exception {
        // GIVEN
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(List.of(custId));
        when(invoiceRepo.findById(testId)).thenReturn(Optional.of(testInvoice));

        // WHEN
        Invoice result = invoiceService.getInvoiceById(testId, mockUser);

        // THEN
        assertEquals(testInvoice, result);
        verify(invoiceRepo).findById(testId);
    }

    @Test
    void getInvoiceById_shouldThrow_whenInvoiceNotFound() {
        // GIVEN
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(List.of(custId));
        when(invoiceRepo.findById(testId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(InvoiceNotFoundException.class,
                () -> invoiceService.getInvoiceById(testId, mockUser));
        verify(invoiceRepo).findById(testId);
    }

    @Test
    void getInvoiceById_shouldThrow_whenUnauthorized() {
        // GIVEN
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(List.of("other-cust"));
        when(invoiceRepo.findById(testId)).thenReturn(Optional.of(testInvoice));

        // WHEN & THEN
        assertThrows(IllegalAccessException.class,
                () -> invoiceService.getInvoiceById(testId, mockUser));
        verify(invoiceRepo).findById(testId);
    }

    @Test
    void updateInvoice_shouldUpdateInvoice_whenAuthorized() throws Exception {
        // GIVEN
        AppUser mockUser = new AppUser();
        mockUser.setCustomerIds(List.of(custId));
        double amountPaid = testAmountDue;
        InvoiceUpdateDTO updateDTO = new InvoiceUpdateDTO(testId, amountPaid);
        Invoice expectedInvoice = testInvoice.withPaid(true).withAmountPaid(amountPaid);

        when(invoiceRepo.findById(testId)).thenReturn(Optional.of(testInvoice));
        when(invoiceRepo.save(expectedInvoice)).thenReturn(expectedInvoice);

        // WHEN
        InvoiceUpdateDTO result = invoiceService.updateInvoice(updateDTO, mockUser);

        // THEN
        assertEquals(updateDTO, result);
        verify(invoiceRepo).findById(testId);
        verify(invoiceRepo).save(expectedInvoice);
    }

    @Test
    void generateInvoice_shouldCreateNewInvoice() {
        // GIVEN
        String newId = "new123";
        InvoiceDTO invoiceDTO = new InvoiceDTO(custId, testSubscriptionId, testDueDate, testAmountDue);

        when(idService.randomId()).thenReturn(newId);
        when(invoiceRepo.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        invoiceService.generateInvoice(invoiceDTO);

        // THEN
        verify(idService).randomId();
        verify(invoiceRepo).save(any(Invoice.class));
    }

    @Test
    void getInvoice_shouldReturnInvoice_whenFound() {
        // GIVEN
        when(invoiceRepo.findBySubscriptionIdAndDueDate(testSubscriptionId, testDueDate))
                .thenReturn(testInvoice);

        // WHEN
        Invoice result = invoiceService.getInvoice(testSubscriptionId, testDueDate);

        // THEN
        assertEquals(testInvoice, result);
        verify(invoiceRepo).findBySubscriptionIdAndDueDate(testSubscriptionId, testDueDate);
    }

    @Test
    void getInvoice_shouldReturnNull_whenNotFound() {
        // GIVEN
        when(invoiceRepo.findBySubscriptionIdAndDueDate("nonexistent", testDueDate))
                .thenReturn(null);

        // WHEN
        Invoice result = invoiceService.getInvoice("nonexistent", testDueDate);

        // THEN
        assertNull(result);
        verify(invoiceRepo).findBySubscriptionIdAndDueDate("nonexistent", testDueDate);
    }
}