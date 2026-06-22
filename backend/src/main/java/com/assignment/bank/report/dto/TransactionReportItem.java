package com.assignment.bank.report.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionReportItem(
        UUID uuid,
        BigDecimal sourceAmount,
        BigDecimal convertedAmount,
        BigDecimal exchangeRate,
        String currency,
        String targetCurrency,
        BigDecimal balance,
        String type,
        String description,
        LocalDateTime timestamp
) {
}