package com.vehicle.invoice.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.image.ImageDataFactory;
import com.vehicle.invoice.model.Invoice;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

@Component
public class PDFGenerator {
    
    public byte[] generateInvoicePDF(Invoice invoice) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        addHeader(document, invoice);
        addDealerCustomerInfo(document, invoice);
        addVehicleDetails(document, invoice);
        addPricingInfo(document, invoice);
        addQRCode(document, invoice);
        addFooter(document, invoice);
        
        document.close();
        return baos.toByteArray();
    }
    
    private void addHeader(Document document, Invoice invoice) {
        Paragraph title = new Paragraph("VEHICLE SALE INVOICE")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        
        Paragraph subtitle = new Paragraph("(Tax Invoice under GST)")
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
        
        Paragraph invoiceNumber = new Paragraph("Invoice #: " + invoice.getInvoiceNumber())
                .setFontSize(12)
                .setTextAlignment(TextAlignment.RIGHT);
        
        Paragraph date = new Paragraph("Date: " + invoice.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.RIGHT);
        
        document.add(title);
        document.add(subtitle);
        document.add(invoiceNumber);
        document.add(date);
        document.add(new Paragraph("\n"));
    }
    
    private void addDealerCustomerInfo(Document document, Invoice invoice) {
        Table infoTable = new Table(2).useAllAvailableWidth();
        
        // Dealer Information
        Cell dealerCell = new Cell();
        dealerCell.add(new Paragraph("DEALER INFORMATION").setBold());
        dealerCell.add(new Paragraph(invoice.getDealer().getName()));
        dealerCell.add(new Paragraph(invoice.getDealer().getAddress()));
        dealerCell.add(new Paragraph("Phone: " + invoice.getDealer().getPhone()));
        dealerCell.add(new Paragraph("Email: " + invoice.getDealer().getEmail()));
        dealerCell.add(new Paragraph("GSTIN: " + invoice.getDealer().getGstNumber()));
        
        // Customer Information
        Cell customerCell = new Cell();
        customerCell.add(new Paragraph("CUSTOMER INFORMATION").setBold());
        customerCell.add(new Paragraph(invoice.getCustomerName()));
        customerCell.add(new Paragraph(invoice.getCustomerAddress()));
        customerCell.add(new Paragraph("Phone: " + invoice.getCustomerPhone()));
        customerCell.add(new Paragraph("Email: " + invoice.getCustomerEmail()));
        
        infoTable.addCell(dealerCell);
        infoTable.addCell(customerCell);
        
        document.add(infoTable);
        document.add(new Paragraph("\n"));
    }
    
    private void addVehicleDetails(Document document, Invoice invoice) {
        Paragraph vehicleTitle = new Paragraph("VEHICLE DETAILS").setBold().setFontSize(14);
        document.add(vehicleTitle);
        
        Table vehicleTable = new Table(2).useAllAvailableWidth();
        
        vehicleTable.addCell(new Cell().add(new Paragraph("Make:")).setBold());
        vehicleTable.addCell(new Cell().add(new Paragraph(invoice.getVehicle().getMake())));
        
        vehicleTable.addCell(new Cell().add(new Paragraph("Model:")).setBold());
        vehicleTable.addCell(new Cell().add(new Paragraph(invoice.getVehicle().getModel())));
        
        vehicleTable.addCell(new Cell().add(new Paragraph("Year:")).setBold());
        vehicleTable.addCell(new Cell().add(new Paragraph(invoice.getVehicle().getYear().toString())));
        
        vehicleTable.addCell(new Cell().add(new Paragraph("Registration Number:")).setBold());
        vehicleTable.addCell(new Cell().add(new Paragraph(invoice.getVehicle().getRegistrationNumber())));
        
        vehicleTable.addCell(new Cell().add(new Paragraph("Price:")).setBold());
        vehicleTable.addCell(new Cell().add(new Paragraph("₹" + invoice.getVehicle().getPrice().toString())));
        
        document.add(vehicleTable);
        document.add(new Paragraph("\n"));
    }
    
    private void addPricingInfo(Document document, Invoice invoice) {
        Paragraph pricingTitle = new Paragraph("PRICING INFORMATION").setBold().setFontSize(14);
        document.add(pricingTitle);
        
        Table pricingTable = new Table(2).useAllAvailableWidth();
        
        pricingTable.addCell(new Cell().add(new Paragraph("Subtotal:")).setBold());
        pricingTable.addCell(new Cell().add(new Paragraph("₹" + invoice.getSubtotal().toString())).setTextAlignment(TextAlignment.RIGHT));
        
        pricingTable.addCell(new Cell().add(new Paragraph("Total GST (18%):")).setBold());
        pricingTable.addCell(new Cell().add(new Paragraph("₹" + invoice.getTotalGST().toString())).setTextAlignment(TextAlignment.RIGHT));
        
        pricingTable.addCell(new Cell().add(new Paragraph("TOTAL:")).setBold().setFontSize(12));
        pricingTable.addCell(new Cell().add(new Paragraph("₹" + invoice.getTotalAmount().toString())).setBold().setFontSize(12).setTextAlignment(TextAlignment.RIGHT));
        
        document.add(pricingTable);
        document.add(new Paragraph("\n"));
    }
    
    private void addQRCode(Document document, Invoice invoice) {
        try {
            QRCodeGenerator qrGenerator = new QRCodeGenerator();
            byte[] qrCodeBytes = qrGenerator.generateQRCode(invoice.getQrCodeData(), 150, 150);
            
            Image qrImage = new Image(ImageDataFactory.create(qrCodeBytes));
            qrImage.setWidth(150);
            qrImage.setHeight(150);
            
            Paragraph qrTitle = new Paragraph("Transaction QR Code").setBold().setFontSize(12);
            qrTitle.setTextAlignment(TextAlignment.CENTER);
            
            document.add(qrTitle);
            document.add(qrImage);
            document.add(new Paragraph("GSTIN: " + invoice.getDealer().getGstNumber()).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Transaction ID: " + invoice.getTransactionId()).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));
            
        } catch (Exception e) {
            document.add(new Paragraph("QR Code generation failed: " + e.getMessage()));
        }
    }
    
    private void addFooter(Document document, Invoice invoice) {
        Paragraph footer = new Paragraph("Thank you for your purchase!")
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
        
        Paragraph terms = new Paragraph("Terms and conditions apply. This invoice is valid for 30 days.")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER);
        
        Paragraph gstNote = new Paragraph("This is a computer generated invoice. No signature required.")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER);
        
        document.add(footer);
        document.add(terms);
        document.add(gstNote);
    }
}
