package com.assignment.bank.report.mapper;

import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.report.dto.TransactionReportItemResponse;
import com.assignment.bank.transaction.entity.Transaction;
import com.assignment.bank.transaction.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionReportMapperTest {

    private final TransactionReportMapper mapper = new TransactionReportMapper();

    @Test
    void shouldMapWithoutCurrencyConversion() {
        Transaction t = mock(Transaction.class);
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        when(t.getUuid()).thenReturn(uuid);
        when(t.getSourceAmount()).thenReturn(new BigDecimal("100.00"));
        when(t.getSourceCurrency()).thenReturn(Currency.EUR);
        when(t.getTargetCurrency()).thenReturn(Currency.EUR);
        when(t.getBalanceAfter()).thenReturn(new BigDecimal("500.00"));
        when(t.getType()).thenReturn(TransactionType.CREDIT);
        when(t.getDescription()).thenReturn("Test");
        when(t.getCreatedAt()).thenReturn(now);

        TransactionReportItemResponse result = mapper.mapToReportItem(t);

        assertNotNull(result);
        assertEquals(uuid, result.uuid());
        assertEquals("EUR", result.currency());
        assertNull(result.targetCurrency());
        assertNull(result.convertedAmount());
        assertNull(result.exchangeRate());
    }

    @Test
    void shouldMapWithCurrencyConversion() {
        Transaction t = mock(Transaction.class);

        when(t.getSourceCurrency()).thenReturn(Currency.USD);
        when(t.getTargetCurrency()).thenReturn(Currency.EUR);
        when(t.getConvertedAmount()).thenReturn(new BigDecimal("85.00"));
        when(t.getExchangeRate()).thenReturn(new BigDecimal("0.85"));
        when(t.getUuid()).thenReturn(UUID.randomUUID());
        when(t.getSourceAmount()).thenReturn(new BigDecimal("100.00"));
        when(t.getBalanceAfter()).thenReturn(new BigDecimal("500.00"));
        when(t.getType()).thenReturn(TransactionType.DEBIT);
        when(t.getDescription()).thenReturn("Conversion");
        when(t.getCreatedAt()).thenReturn(LocalDateTime.now());

        TransactionReportItemResponse result = mapper.mapToReportItem(t);

        assertEquals("USD", result.currency());
        assertEquals("EUR", result.targetCurrency());
        assertEquals(new BigDecimal("85.00"), result.convertedAmount());
        assertEquals(new BigDecimal("0.85"), result.exchangeRate());
    }
}