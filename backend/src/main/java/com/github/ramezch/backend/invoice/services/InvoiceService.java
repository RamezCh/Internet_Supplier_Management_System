package com.github.ramezch.backend.invoice.services;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.exceptions.InvoiceNotFoundException;
import com.github.ramezch.backend.invoice.models.Invoice;
import com.github.ramezch.backend.invoice.models.InvoiceDTO;
import com.github.ramezch.backend.invoice.models.InvoiceUpdateDTO;
import com.github.ramezch.backend.invoice.repository.InvoiceRepository;
import com.github.ramezch.backend.utils.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepo;
    private final IdService idService;

    private void  checkIfAppUserContainsCustomerId(String customerId, AppUser appUser) throws IllegalAccessException {
        if(!appUser.getCustomerIds().contains(customerId)) {
            throw new IllegalAccessException(String.format("Customer with id: %s not found", customerId));
        }
    }

    public List<Invoice> getInvoicesByCustomerId(String customerId, AppUser appUser) throws IllegalAccessException {
        checkIfAppUserContainsCustomerId(customerId, appUser);
        return invoiceRepo.findAllByCustomerId(customerId);
    }

    public Invoice getInvoiceById(String invoiceID, AppUser appUser) throws IllegalAccessException {
        Optional<Invoice> invoiceOptional = invoiceRepo.findById(invoiceID);
        if(invoiceOptional.isEmpty()) {
            throw new InvoiceNotFoundException(invoiceID);
        }
        Invoice invoice = invoiceOptional.get();
        String customerId = invoice.customerId();
        checkIfAppUserContainsCustomerId(customerId, appUser);
        return invoice;
    }

    public InvoiceUpdateDTO updateInvoice(InvoiceUpdateDTO invoiceDTO, AppUser appUser) throws IllegalAccessException {
        Invoice existingInvoice = invoiceRepo.findById(invoiceDTO.id()).orElseThrow(() -> new InvoiceNotFoundException(invoiceDTO.id()));
        checkIfAppUserContainsCustomerId(existingInvoice.customerId(), appUser);
        if(invoiceDTO.amountPaid() == existingInvoice.amountDue()) {
            Invoice newInvoiceToSave = existingInvoice.withPaid(true).withAmountPaid(invoiceDTO.amountPaid());
            invoiceRepo.save(newInvoiceToSave);
        }
        return invoiceDTO;
    }

    // These are used in Scheduler and other backend codes
    public Invoice getInvoice(String subscriptionId, Instant subscriptionEndDate) {
        return invoiceRepo.findBySubscriptionIdAndDueDate(subscriptionId, subscriptionEndDate);
    }

    public void generateInvoice(InvoiceDTO invoiceDTO) {
        String invoiceID = idService.randomId();
        Instant issueDate = Instant.now();
        Invoice newInvoice = new Invoice(invoiceID, invoiceDTO.customerId(), invoiceDTO.subscriptionId(), issueDate, invoiceDTO.dueDate(), invoiceDTO.amountDue(), 0, false);
        invoiceRepo.save(newInvoice);
    }

}
