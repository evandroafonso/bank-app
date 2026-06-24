package com.assignment.bank.report.service;

import com.assignment.bank.report.dto.TransactionReportItemResponse;
import com.assignment.bank.report.dto.TransactionReportRequest;
import com.assignment.bank.report.dto.TransactionReportResponse;
import com.assignment.bank.report.mapper.TransactionReportMapper;
import com.assignment.bank.transaction.entity.Transaction;
import com.assignment.bank.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionReportServiceTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private TransactionPdfService pdfService;

    @Mock
    private TransactionReportMapper reportMapper;

    @InjectMocks
    private TransactionReportService service;

    @Test
    void shouldGenerateReportSuccessfully() {
        UUID uuid = UUID.randomUUID();
        Transaction transaction = mock(Transaction.class);

        when(transaction.getUuid()).thenReturn(uuid);
        when(repository.findByUuid(uuid)).thenReturn(Optional.of(transaction));

        TransactionReportItemResponse mockMappedItem = mock(TransactionReportItemResponse.class);
        when(reportMapper.mapToReportItem(transaction)).thenReturn(mockMappedItem);

        when(pdfService.generatePdf(any(TransactionReportItemResponse.class)))
                .thenReturn("pdf-content".getBytes());

        TransactionReportRequest request = new TransactionReportRequest(uuid);

        TransactionReportResponse response = service.generate(request);

        assertNotNull(response);
        assertNotNull(response.pdf());
        assertEquals("transaction-" + uuid + ".pdf", response.fileName());

        verify(repository).findByUuid(uuid);
        verify(reportMapper).mapToReportItem(transaction);
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

        assertTrue(ex.getMessage().contains("Transaction not found"));
        assertTrue(ex.getMessage().contains(uuid.toString()));

        verify(repository).findByUuid(uuid);
        verifyNoInteractions(reportMapper);
        verifyNoInteractions(pdfService);
    }

    @Test
    void shouldMapTransactionCorrectlyBeforeGeneratingPdf() {
        UUID uuid = UUID.randomUUID();
        Transaction transaction = mock(Transaction.class);

        when(repository.findByUuid(uuid)).thenReturn(Optional.of(transaction));

        TransactionReportItemResponse mockMappedItem = mock(TransactionReportItemResponse.class);
        when(mockMappedItem.uuid()).thenReturn(uuid);
        when(mockMappedItem.sourceAmount()).thenReturn(new BigDecimal("150.00"));
        when(mockMappedItem.targetCurrency()).thenReturn("USD");
        when(mockMappedItem.type()).thenReturn("DEBIT");
        when(mockMappedItem.description()).thenReturn("Withdrawal");

        when(reportMapper.mapToReportItem(transaction)).thenReturn(mockMappedItem);

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