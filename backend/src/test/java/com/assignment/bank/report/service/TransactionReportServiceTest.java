package com.assignment.bank.report.service;

import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.report.dto.TransactionReportItemResponse;
import com.assignment.bank.report.dto.TransactionReportRequest;
import com.assignment.bank.report.dto.TransactionReportResponse;
import com.assignment.bank.transaction.entity.Transaction;
import com.assignment.bank.transaction.enums.TransactionType;
import com.assignment.bank.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionReportServiceTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private TransactionPdfService pdfService;

    @InjectMocks
    private TransactionReportService service;

    @Test
    void shouldGenerateReportSuccessfully() {

        UUID uuid = UUID.randomUUID();

        Transaction transaction = mock(Transaction.class);

        when(transaction.getUuid()).thenReturn(uuid);
        when(transaction.getSourceAmount()).thenReturn(new BigDecimal("100.00"));
        when(transaction.getConvertedAmount()).thenReturn(new BigDecimal("100.00"));
        when(transaction.getExchangeRate()).thenReturn(new BigDecimal("1.00"));
        when(transaction.getSourceCurrency()).thenReturn(Currency.EUR);
        when(transaction.getTargetCurrency()).thenReturn(Currency.EUR);
        when(transaction.getBalanceAfter()).thenReturn(new BigDecimal("500.00"));
        when(transaction.getType()).thenReturn(TransactionType.CREDIT);
        when(transaction.getDescription()).thenReturn("Test transaction");
        when(transaction.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(repository.findByUuid(uuid)).thenReturn(Optional.of(transaction));
        when(pdfService.generatePdf(any(TransactionReportItemResponse.class)))
                .thenReturn("pdf-content".getBytes());

        TransactionReportRequest request = new TransactionReportRequest(uuid);

        TransactionReportResponse response = service.generate(request);

        assertNotNull(response);
        assertNotNull(response.pdf());
        assertEquals("transaction-" + uuid + ".pdf", response.fileName());

        verify(repository).findByUuid(uuid);
        verify(pdfService).generatePdf(any(TransactionReportItemResponse.class));
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {

        UUID uuid = UUID.randomUUID();

        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        TransactionReportRequest request = new TransactionReportRequest(uuid);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.generate(request)
        );

        assertEquals("Transaction not found", ex.getMessage());

        verify(repository).findByUuid(uuid);
        verifyNoInteractions(pdfService);
    }

    @Test
    void shouldMapTransactionCorrectlyBeforeGeneratingPdf() {

        UUID uuid = UUID.randomUUID();

        Transaction transaction = mock(Transaction.class);

        when(transaction.getUuid()).thenReturn(uuid);
        when(transaction.getSourceAmount()).thenReturn(new BigDecimal("150.00"));
        when(transaction.getConvertedAmount()).thenReturn(new BigDecimal("150.00"));
        when(transaction.getExchangeRate()).thenReturn(new BigDecimal("1.00"));
        when(transaction.getSourceCurrency()).thenReturn(Currency.EUR);
        when(transaction.getTargetCurrency()).thenReturn(Currency.USD);
        when(transaction.getBalanceAfter()).thenReturn(new BigDecimal("350.00"));
        when(transaction.getType()).thenReturn(TransactionType.DEBIT);
        when(transaction.getDescription()).thenReturn("Withdrawal");
        when(transaction.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(repository.findByUuid(uuid)).thenReturn(Optional.of(transaction));

        when(pdfService.generatePdf(any(TransactionReportItemResponse.class)))
                .thenReturn("pdf".getBytes());

        TransactionReportRequest request = new TransactionReportRequest(uuid);

        service.generate(request);

        verify(pdfService).generatePdf(argThat(item ->
                item.uuid().equals(uuid) &&
                        item.sourceAmount().compareTo(new BigDecimal("150.00")) == 0 &&
                        item.targetCurrency().equals("USD") &&
                        item.type().equals("DEBIT") &&
                        item.description().equals("Withdrawal")
        ));
    }
}