package com.vehicle.invoice.service;

import com.vehicle.invoice.model.*;
import com.vehicle.invoice.repository.*;
import com.vehicle.invoice.util.PDFGenerator;
import com.vehicle.invoice.util.QRCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InvoiceService {
    
    @Autowired
    private DealerRepository dealerRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private PDFGenerator pdfGenerator;
    
    @Autowired
    private QRCodeGenerator qrCodeGenerator;
    
    public byte[] generateInvoicePDF(InvoiceRequest request) throws IOException {
        // Validate and fetch dealer and vehicle
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new RuntimeException("Dealer not found with ID: " + request.getDealerId()));
        
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + request.getVehicleId()));
        
        // Generate invoice number and transaction ID
        String invoiceNumber = generateInvoiceNumber();
        String transactionId = generateTransactionId();
        
        // Calculate pricing with Indian GST
        BigDecimal subtotal = vehicle.getPrice();
        BigDecimal gstRate = new BigDecimal("0.18"); // 18% GST
        BigDecimal totalGST = subtotal.multiply(gstRate);
        BigDecimal totalAmount = subtotal.add(totalGST);
        
        // Create invoice entity
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setTimestamp(LocalDateTime.now());
        invoice.setCustomerName(request.getCustomerName());
        invoice.setCustomerAddress(request.getCustomerAddress());
        invoice.setCustomerPhone(request.getCustomerPhone());
        invoice.setCustomerEmail(request.getCustomerEmail());
        invoice.setDealer(dealer);
        invoice.setVehicle(vehicle);
        invoice.setSubtotal(subtotal);
        invoice.setTotalGST(totalGST);
        invoice.setTotalAmount(totalAmount);
        invoice.setTransactionId(transactionId);
        invoice.setQrCodeData(generateQRCodeData(invoice));
        
        // Save invoice to database
        invoiceRepository.save(invoice);
        
        // Generate PDF
        return pdfGenerator.generateInvoicePDF(invoice);
    }
    
    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }
    
    private String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
    
    private String generateQRCodeData(Invoice invoice) {
        return String.format("GSTIN:%s|Invoice:%s|Amount:₹%s|GST:₹%s|Date:%s",
                invoice.getDealer().getGstNumber(),
                invoice.getInvoiceNumber(),
                invoice.getTotalAmount().toString(),
                invoice.getTotalGST().toString(),
                invoice.getTimestamp().toString());
    }
}
