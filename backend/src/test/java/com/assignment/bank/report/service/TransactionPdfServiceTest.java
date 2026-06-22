package com.assignment.bank.report.service;

import com.assignment.bank.report.dto.TransactionReportItem;
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

        TransactionReportItem item = new TransactionReportItem(
                UUID.randomUUID(),
                new BigDecimal("100.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                "EUR",
                "EUR",
                new BigDecimal("500.0000"),
                "CREDIT",
                "Test transaction",
                LocalDateTime.now()
        );

        byte[] pdf = transactionPdfService.generatePdf(item);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void shouldGeneratePdfEvenWithNullDescription() {

        TransactionReportItem item = new TransactionReportItem(
                UUID.randomUUID(),
                new BigDecimal("100.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                "EUR",
                "EUR",
                new BigDecimal("500.0000"),
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

        TransactionReportItem item = new TransactionReportItem(
                UUID.randomUUID(),
                new BigDecimal("100.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                "EUR",
                "EUR",
                new BigDecimal("500.0000"),
                "CREDIT",
                "Test",
                LocalDateTime.now()
        );

        TransactionPdfService serviceSpy = org.mockito.Mockito.spy(transactionPdfService);

        org.mockito.Mockito.doThrow(new RuntimeException("PDF error"))
                .when(serviceSpy).generatePdf(item);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> serviceSpy.generatePdf(item)
        );

        assertEquals("PDF error", ex.getMessage());
    }
}