package com.assignment.bank.report.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionReportItemResponse(
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