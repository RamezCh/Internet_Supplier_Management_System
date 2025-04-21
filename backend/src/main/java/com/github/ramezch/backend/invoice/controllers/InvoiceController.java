package com.github.ramezch.backend.invoice.controllers;


import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.invoice.models.Invoice;
import com.github.ramezch.backend.invoice.models.InvoiceUpdateDTO;
import com.github.ramezch.backend.invoice.services.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;

    @GetMapping("/customer/{customerId}")
    public List<Invoice> getCustomerInvoices(@PathVariable String customerId, @AuthenticationPrincipal AppUser appUser) throws IllegalAccessException {
        return invoiceService.getInvoicesByCustomerId(customerId, appUser);
    }

    @GetMapping("{invoiceId}")
    public Invoice getInvoice(@PathVariable String invoiceId, @AuthenticationPrincipal AppUser appUser) throws IllegalAccessException {
        return invoiceService.getInvoiceById(invoiceId, appUser);
    }

    @PutMapping
    public InvoiceUpdateDTO updateInvoice(@Valid @RequestBody InvoiceUpdateDTO invoiceDTO, @AuthenticationPrincipal AppUser appUser) throws IllegalAccessException {
        return invoiceService.updateInvoice(invoiceDTO, appUser);
    }

}
