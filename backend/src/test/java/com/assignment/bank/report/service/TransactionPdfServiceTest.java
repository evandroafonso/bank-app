package com.assignment.bank.report.service;

import com.assignment.bank.report.dto.TransactionReportItemResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionPdfServiceTest {

    @InjectMocks
    private TransactionPdfService transactionPdfService;

    @Test
    void shouldGeneratePdfSuccessfully() {
        TransactionReportItemResponse item = new TransactionReportItemResponse(
                UUID.randomUUID(),
                new BigDecimal("100.0044"),
                new BigDecimal("100.0055"),
                new BigDecimal("1.0000"),
                "EUR",
                "EUR",
                new BigDecimal("500.9999"),
                "CREDIT",
                "Test transaction",
                LocalDateTime.now()
        );

        byte[] pdf = transactionPdfService.generatePdf(item);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void shouldGeneratePdfEvenWithNullOptionalFields() {
        TransactionReportItemResponse item = new TransactionReportItemResponse(
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                null,
                null,
                "EUR",
                null,
                new BigDecimal("500.00"),
                "CREDIT",
                null,
                LocalDateTime.now()
        );

        byte[] pdf = transactionPdfService.generatePdf(item);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenPdfFails() {
        TransactionReportItemResponse item = new TransactionReportItemResponse(
                null,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                new BigDecimal("1.00"),
                "EUR",
                "EUR",
                new BigDecimal("500.00"),
                "CREDIT",
                "Test",
                LocalDateTime.now()
        );

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> transactionPdfService.generatePdf(item)
        );

        assertEquals("Error generating PDF", ex.getMessage());
        assertNotNull(ex.getCause());
    }
}