package com.github.ramezch.backend.invoice.services;

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

    @InjectMocks
    private InvoiceService invoiceService;

    private Invoice testInvoice;
    private final String testId = "test123";
    private final String testSubscriptionId = "sub123";
    private final Instant testDueDate = Instant.now().plusSeconds(86400);
    private final Instant testIssueDate = Instant.now();
    private final double testAmountDue = 100.0;

    @BeforeEach
    void setUp() {
        testInvoice = new Invoice(testId, testSubscriptionId, testIssueDate, testDueDate, testAmountDue, 0, false);
    }

    @Test
    void getInvoices_shouldReturnAllInvoices() {
        // Given
        when(invoiceRepo.findAll()).thenReturn(List.of(testInvoice));

        // When
        List<Invoice> result = invoiceService.getInvoices();

        // Then
        assertEquals(1, result.size());
        assertEquals(testInvoice, result.getFirst());
        verify(invoiceRepo, times(1)).findAll();
    }

    @Test
    void getInvoicesBySubscriptionId_shouldReturnMatchingInvoices() {
        // Given
        when(invoiceRepo.findBySubscriptionId(testSubscriptionId)).thenReturn(List.of(testInvoice));

        // When
        List<Invoice> result = invoiceService.getInvoicesBySubscriptionId(testSubscriptionId);

        // Then
        assertEquals(1, result.size());
        assertEquals(testInvoice, result.getFirst());
        verify(invoiceRepo, times(1)).findBySubscriptionId(testSubscriptionId);
    }

    @Test
    void getInvoice_shouldReturnInvoiceWhenFound() {
        // Given
        when(invoiceRepo.findBySubscriptionIdAndDueDate(testSubscriptionId, testDueDate))
                .thenReturn(testInvoice);

        // When
        Invoice result = invoiceService.getInvoice(testSubscriptionId, testDueDate);

        // Then
        assertEquals(testInvoice, result);
        verify(invoiceRepo, times(1)).findBySubscriptionIdAndDueDate(testSubscriptionId, testDueDate);
    }

    @Test
    void getInvoice_shouldReturnNullWhenNotFound() {
        // Given
        when(invoiceRepo.findBySubscriptionIdAndDueDate("nonexistent", testDueDate))
                .thenReturn(null);

        // When
        Invoice result = invoiceService.getInvoice("nonexistent", testDueDate);

        // Then
        assertNull(result);
        verify(invoiceRepo, times(1)).findBySubscriptionIdAndDueDate("nonexistent", testDueDate);
    }

    @Test
    void generateInvoice_shouldCreateNewInvoice() {
        // Given
        String newId = "new123";
        InvoiceDTO invoiceDTO = new InvoiceDTO(testSubscriptionId, testDueDate, testAmountDue);
        Invoice expectedInvoice = new Invoice(newId, testSubscriptionId, testIssueDate, testDueDate, testAmountDue, 0, false);

        when(idService.randomId()).thenReturn(newId);
        when(invoiceRepo.save(any(Invoice.class))).thenReturn(expectedInvoice);

        // When
        invoiceService.generateInvoice(invoiceDTO);

        // Then
        verify(idService, times(1)).randomId();
        verify(invoiceRepo, times(1)).save(any(Invoice.class));
    }

    @Test
    void updateInvoice_shouldUpdateInvoiceWhenFullAmountPaid() {
        // Given
        double amountPaid = testAmountDue;
        InvoiceUpdateDTO updateDTO = new InvoiceUpdateDTO(testId, amountPaid);
        Invoice expectedUpdatedInvoice = testInvoice.withPaid(true).withAmountPaid(amountPaid);

        when(invoiceRepo.findById(testId)).thenReturn(Optional.of(testInvoice));
        when(invoiceRepo.save(expectedUpdatedInvoice)).thenReturn(expectedUpdatedInvoice);

        // When
        InvoiceUpdateDTO result = invoiceService.updateInvoice(updateDTO);

        // Then
        assertEquals(updateDTO, result);
        verify(invoiceRepo, times(1)).findById(testId);
        verify(invoiceRepo, times(1)).save(expectedUpdatedInvoice);
    }

    @Test
    void updateInvoice_shouldNotMarkAsPaidWhenPartialAmountPaid() {
        // Given
        double amountPaid = testAmountDue - 10;
        InvoiceUpdateDTO updateDTO = new InvoiceUpdateDTO(testId, amountPaid);
        Invoice expectedUpdatedInvoice = testInvoice.withAmountPaid(amountPaid);

        when(invoiceRepo.findById(testId)).thenReturn(Optional.of(testInvoice));

        // When
        InvoiceUpdateDTO result = invoiceService.updateInvoice(updateDTO);

        // Then
        assertEquals(updateDTO, result);
        verify(invoiceRepo, times(1)).findById(testId);
        assertFalse(expectedUpdatedInvoice.isPaid());
    }

    @Test
    void updateInvoice_shouldThrowExceptionWhenInvoiceNotFound() {
        // Given
        String nonExistentId = "nonexistent";
        InvoiceUpdateDTO updateDTO = new InvoiceUpdateDTO(nonExistentId, testAmountDue);

        when(invoiceRepo.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvoiceNotFoundException.class, () -> invoiceService.updateInvoice(updateDTO));
        verify(invoiceRepo, times(1)).findById(nonExistentId);
        verify(invoiceRepo, never()).save(any());
    }
}