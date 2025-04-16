package com.github.ramezch.backend.invoice.services;

import com.github.ramezch.backend.exceptions.InvoiceNotFoundException;
import com.github.ramezch.backend.invoice.models.Invoice;
import com.github.ramezch.backend.invoice.models.InvoiceDTO;
import com.github.ramezch.backend.invoice.models.InvoiceUpdateDTO;
import com.github.ramezch.backend.invoice.repository.InvoiceRepository;
import com.github.ramezch.backend.utils.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepo;
    private final IdService idService;

    public Invoice getInvoice(String subscriptionId, Instant subscriptionEndDate) {
        return invoiceRepo.findBySubscriptionIdAndDueDate(subscriptionId, subscriptionEndDate);
    }

    public void generateInvoice(InvoiceDTO invoiceDTO) {
        String invoiceID = idService.randomId();
        Instant issueDate = Instant.now();
        Invoice newInvoice = new Invoice(invoiceID, invoiceDTO.subscriptionId(), issueDate, invoiceDTO.dueDate(), invoiceDTO.amountDue(), 0, false);
        invoiceRepo.save(newInvoice);
    }

    public InvoiceUpdateDTO updateInvoice(InvoiceUpdateDTO invoiceDTO) {
        Invoice existingInvoice = invoiceRepo.findById(invoiceDTO.id()).orElseThrow(() -> new InvoiceNotFoundException(invoiceDTO.id()));
        if(invoiceDTO.amountPaid() == existingInvoice.amountDue()) {
            Invoice newInvoiceToSave = existingInvoice.withPaid(true).withAmountPaid(invoiceDTO.amountPaid());
            invoiceRepo.save(newInvoiceToSave);
        }
        return invoiceDTO;
    }

}
